package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseSkillAggregationRow;
import com.hlinks.domain.course.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        if (weights.isEmpty()) {
            log.info("강의 스킬 집계 생략. 집계 가능한 스킬이 없습니다. courseId={}", courseId);
            return;
        }

        weights.forEach((skillId, weight) ->
                courseMapper.insertCourseSkill(courseId, skillId, weight)
        );
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

        return normalizeToOne(rawScores);
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

            BigDecimal weight = resolveRawSkillWeight(row.getWeight());

            if (weight == null) {
                continue;
            }

            rawWeights.merge(row.getSkillId(), weight, BigDecimal::add);
        }

        return normalizeToOne(rawWeights);
    }

    private BigDecimal resolveRawSkillWeight(BigDecimal weight) {
        if (weight == null) {
            return BigDecimal.ONE;
        }

        if (weight.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return weight;
    }

    private Map<Long, BigDecimal> normalizeToOne(Map<Long, BigDecimal> rawScores) {
        BigDecimal rawTotal = rawScores.values().stream()
                .filter(weight -> weight != null && weight.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (rawScores.isEmpty() || rawTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of();
        }

        Map<Long, BigDecimal> normalizedWeights = new LinkedHashMap<>();
        List<Map.Entry<Long, BigDecimal>> entries = rawScores.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        BigDecimal accumulatedWeight = BigDecimal.ZERO;

        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<Long, BigDecimal> entry = entries.get(index);
            BigDecimal normalizedWeight;

            if (index == entries.size() - 1) {
                normalizedWeight = BigDecimal.ONE.subtract(accumulatedWeight);
            } else {
                normalizedWeight = entry.getValue().divide(rawTotal, WEIGHT_SCALE, RoundingMode.HALF_UP);
                accumulatedWeight = accumulatedWeight.add(normalizedWeight);
            }

            normalizedWeights.put(entry.getKey(), normalizedWeight.max(BigDecimal.ZERO));
        }

        return normalizedWeights;
    }
}
