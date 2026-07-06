package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseSkillAggregationRow;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.domain.course.type.CourseSkillCoverageLevel;
import com.hlinks.domain.course.util.SkillWeightNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSkillAggregationService {

    private static final int WEIGHT_SCALE = 6;

    private final CourseMapper courseMapper;

    @Transactional
    public int recalculateAllCourseSkills() {
        List<Long> courseIds = courseMapper.findCourseIdsHavingChapterSkills();

        for (Long courseId : courseIds) {
            recalculateCourseSkills(courseId);
        }

        return courseIds.size();
    }

    @Transactional
    public void recalculateCourseSkills(Long courseId) {
        List<CourseSkillAggregationRow> chapterSkills = courseMapper.findChapterSkillsByCourseId(courseId);

        courseMapper.deleteCourseSkillsByCourseId(courseId);

        if (chapterSkills == null || chapterSkills.isEmpty()) {
            log.info("강의 스킬 집계 생략. CHAPTER_SKILL 데이터가 없습니다. courseId={}", courseId);
            return;
        }

        Map<Long, BigDecimal> weights = calculateCourseSkillWeights(chapterSkills);
        Map<Long, AggregatedCoverage> coverages = calculateCourseSkillCoverages(chapterSkills);

        if (weights.isEmpty()) {
            log.info("강의 스킬 집계 생략. 집계 가능한 스킬이 없습니다. courseId={}", courseId);
            return;
        }

        weights.forEach((skillId, weight) -> {
            AggregatedCoverage coverage = coverages.getOrDefault(skillId, AggregatedCoverage.defaultValue());

            courseMapper.insertCourseSkill(
                    courseId,
                    skillId,
                    weight,
                    coverage.level().name(),
                    coverage.reason()
            );
        });
    }

    private Map<Long, BigDecimal> calculateCourseSkillWeights(List<CourseSkillAggregationRow> chapterSkills) {
        Map<Long, Integer> chapterDurations = chapterSkills.stream()
                .collect(Collectors.toMap(
                        CourseSkillAggregationRow::getChapterId,
                        row -> row.getDurationSeconds() == null ? 0 : row.getDurationSeconds(),
                        Math::max,
                        LinkedHashMap::new
                ));

        int totalDuration = chapterDurations.values().stream()
                .filter(duration -> duration != null && duration > 0)
                .mapToInt(Integer::intValue)
                .sum();
        boolean useDurationWeight = totalDuration > 0
                && chapterDurations.values().stream().allMatch(duration -> duration != null && duration > 0);

        Map<Long, List<CourseSkillAggregationRow>> chapterSkillGroups = chapterSkills.stream()
                .filter(row -> row.getChapterId() != null)
                .collect(Collectors.groupingBy(
                        CourseSkillAggregationRow::getChapterId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, BigDecimal> rawScores = new LinkedHashMap<>();
        int chapterCount = Math.max(1, chapterDurations.size());

        for (Map.Entry<Long, List<CourseSkillAggregationRow>> entry : chapterSkillGroups.entrySet()) {
            Long chapterId = entry.getKey();
            List<CourseSkillAggregationRow> rows = entry.getValue();
            Map<Long, BigDecimal> chapterWeights = normalizeChapterSkillWeights(rows);

            if (chapterWeights.isEmpty()) {
                continue;
            }

            BigDecimal chapterRatio = resolveChapterRatio(
                    chapterDurations.get(chapterId),
                    chapterCount,
                    totalDuration,
                    useDurationWeight
            );

            for (Map.Entry<Long, BigDecimal> chapterWeight : chapterWeights.entrySet()) {
                BigDecimal score = chapterWeight.getValue().multiply(chapterRatio);
                rawScores.merge(chapterWeight.getKey(), score, BigDecimal::add);
            }
        }

        return SkillWeightNormalizer.normalizeToOne(rawScores);
    }

    private BigDecimal resolveChapterRatio(
            Integer durationSeconds,
            int chapterCount,
            int totalDuration,
            boolean useDurationWeight
    ) {
        if (!useDurationWeight) {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(chapterCount), WEIGHT_SCALE, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(totalDuration), WEIGHT_SCALE, RoundingMode.HALF_UP);
    }

    private Map<Long, BigDecimal> normalizeChapterSkillWeights(List<CourseSkillAggregationRow> rows) {
        Map<Long, BigDecimal> rawWeights = new LinkedHashMap<>();

        for (CourseSkillAggregationRow row : rows) {
            if (row.getSkillId() == null) {
                continue;
            }

            BigDecimal weight = SkillWeightNormalizer.resolveRawWeight(row.getWeight());

            if (weight == null) {
                continue;
            }

            rawWeights.merge(row.getSkillId(), weight, BigDecimal::add);
        }

        return SkillWeightNormalizer.normalizeToOne(rawWeights);
    }

    private Map<Long, AggregatedCoverage> calculateCourseSkillCoverages(List<CourseSkillAggregationRow> chapterSkills) {
        Map<Long, Map<CourseSkillCoverageLevel, BigDecimal>> coverageScores = new LinkedHashMap<>();
        Map<Long, Map<CourseSkillCoverageLevel, CoverageReasonCandidate>> coverageReasons = new LinkedHashMap<>();

        for (CourseSkillAggregationRow row : chapterSkills) {
            if (row.getSkillId() == null) {
                continue;
            }

            BigDecimal weight = SkillWeightNormalizer.resolveRawWeight(row.getWeight());

            if (weight == null) {
                continue;
            }

            CourseSkillCoverageLevel coverageLevel = CourseSkillCoverageLevel.from(row.getCoverageLevel());
            coverageScores
                    .computeIfAbsent(row.getSkillId(), key -> new LinkedHashMap<>())
                    .merge(coverageLevel, weight, BigDecimal::add);
            mergeCoverageReason(
                    coverageReasons,
                    row.getSkillId(),
                    coverageLevel,
                    weight,
                    row.getCoverageReason()
            );
        }

        Map<Long, AggregatedCoverage> coverages = new LinkedHashMap<>();

        for (Map.Entry<Long, Map<CourseSkillCoverageLevel, BigDecimal>> entry : coverageScores.entrySet()) {
            Long skillId = entry.getKey();
            CourseSkillCoverageLevel selectedLevel = selectCoverageLevel(entry.getValue());
            String reason = resolveCoverageReason(coverageReasons.get(skillId), selectedLevel);

            coverages.put(skillId, new AggregatedCoverage(selectedLevel, reason));
        }

        return coverages;
    }

    private CourseSkillCoverageLevel selectCoverageLevel(Map<CourseSkillCoverageLevel, BigDecimal> scores) {
        CourseSkillCoverageLevel selectedLevel = CourseSkillCoverageLevel.BASIC;
        BigDecimal selectedScore = BigDecimal.ZERO;

        for (Map.Entry<CourseSkillCoverageLevel, BigDecimal> entry : scores.entrySet()) {
            CourseSkillCoverageLevel level = entry.getKey();
            BigDecimal score = entry.getValue();

            if (score == null) {
                continue;
            }

            if (score.compareTo(selectedScore) > 0
                    || (score.compareTo(selectedScore) == 0 && level.getRank() > selectedLevel.getRank())) {
                selectedLevel = level;
                selectedScore = score;
            }
        }

        return selectedLevel;
    }

    private void mergeCoverageReason(
            Map<Long, Map<CourseSkillCoverageLevel, CoverageReasonCandidate>> coverageReasons,
            Long skillId,
            CourseSkillCoverageLevel coverageLevel,
            BigDecimal weight,
            String coverageReason
    ) {
        if (!StringUtils.hasText(coverageReason)) {
            return;
        }

        Map<CourseSkillCoverageLevel, CoverageReasonCandidate> reasonByLevel = coverageReasons
                .computeIfAbsent(skillId, key -> new LinkedHashMap<>());
        CoverageReasonCandidate current = reasonByLevel.get(coverageLevel);

        if (current == null || weight.compareTo(current.weight()) > 0) {
            reasonByLevel.put(coverageLevel, new CoverageReasonCandidate(weight, coverageReason.trim()));
        }
    }

    private String resolveCoverageReason(
            Map<CourseSkillCoverageLevel, CoverageReasonCandidate> reasons,
            CourseSkillCoverageLevel selectedLevel
    ) {
        if (reasons != null && reasons.get(selectedLevel) != null) {
            return reasons.get(selectedLevel).reason();
        }

        return selectedLevel.getDefaultReason();
    }

    private record AggregatedCoverage(CourseSkillCoverageLevel level, String reason) {

        private static AggregatedCoverage defaultValue() {
            return new AggregatedCoverage(
                    CourseSkillCoverageLevel.BASIC,
                    CourseSkillCoverageLevel.BASIC.getDefaultReason()
            );
        }
    }

    private record CoverageReasonCandidate(BigDecimal weight, String reason) {
    }
}
