package com.hlinks.domain.course.service;

import com.hlinks.domain.course.ai.dto.CourseSummarySkill;
import com.hlinks.domain.course.ai.dto.CourseSummaryGenerateResponse;
import com.hlinks.domain.course.ai.service.AiCourseSummaryService;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.course.mapper.CourseChapterSkillMapper;
import com.hlinks.domain.course.type.CourseSkillCoverageLevel;
import com.hlinks.domain.course.util.SkillWeightNormalizer;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseChapterSummaryService {

    private static final String NEW_SKILL_YN = "Y";
    private static final String EXISTING_SKILL_YN = "N";
    private static final String DEFAULT_SKILL_TYPE = "TECH";
    private static final int MIN_SKILL_NAME_LENGTH = 2;
    private static final int MAX_SKILL_NAME_LENGTH = 100;

    private final CourseChapterMapper courseChapterMapper;
    private final CourseChapterSkillMapper courseChapterSkillMapper;
    private final AiCourseSummaryService aiCourseSummaryService;
    private final CourseSkillAggregationService courseSkillAggregationService;

    @Transactional
    public CourseSummaryGenerateResponse generateAndSaveSummary(Long chapterId) {
        CourseSummaryGenerateResponse response = generateSummary(chapterId);
        saveSummaryText(chapterId, response.getSummaryText());
        saveChapterSkills(chapterId, response.getSkills());

        return response;
    }

    public CourseSummaryGenerateResponse generateSummary(Long chapterId) {
        CourseChapter chapter = findChapter(chapterId);

        if (!StringUtils.hasText(chapter.getTranscriptText())) {
            throw new BaseException(
                    ErrorResponseCode.BAD_REQUEST,
                    "챕터 transcript가 존재하지 않습니다. chapterId=" + chapterId
            );
        }

        return aiCourseSummaryService.generateSummary(chapter.getTranscriptText());
    }

    @Transactional
    public void saveSummaryText(Long chapterId, String summaryText) {
        int updatedCount = courseChapterMapper.updateSummaryText(chapterId, summaryText);

        if (updatedCount == 0) {
            throw new BaseException(
                    ErrorResponseCode.NOT_FOUND_ENDPOINT,
                    "요약을 저장할 챕터를 찾을 수 없습니다. chapterId=" + chapterId
            );
        }
    }

    @Transactional
    public void saveChapterSkills(Long chapterId, List<CourseSummarySkill> skills) {
        CourseChapter chapter = findChapter(chapterId);

        courseChapterSkillMapper.deleteByChapterId(chapterId);

        if (skills == null || skills.isEmpty()) {
            courseSkillAggregationService.recalculateCourseSkills(chapter.getCourseId());
            return;
        }

        Map<Long, BigDecimal> matchedSkills = new LinkedHashMap<>();
        Map<Long, Map<CourseSkillCoverageLevel, BigDecimal>> coverageScores = new LinkedHashMap<>();
        Map<Long, Map<CourseSkillCoverageLevel, CoverageReasonCandidate>> coverageReasons = new LinkedHashMap<>();

        for (CourseSummarySkill skill : skills) {
            Long skillId = resolveSkillId(chapterId, skill);

            if (skillId == null) {
                continue;
            }

            BigDecimal weight = SkillWeightNormalizer.resolveRawWeight(skill.getWeight());

            if (weight == null) {
                continue;
            }

            matchedSkills.merge(skillId, weight, BigDecimal::add);
            CourseSkillCoverageLevel coverageLevel = CourseSkillCoverageLevel.from(skill.getCoverageLevel());
            coverageScores
                    .computeIfAbsent(skillId, key -> new LinkedHashMap<>())
                    .merge(coverageLevel, weight, BigDecimal::add);
            mergeCoverageReason(coverageReasons, skillId, coverageLevel, weight, skill.getCoverageReason());
        }

        Map<Long, BigDecimal> normalizedSkills = SkillWeightNormalizer.normalizeToOne(matchedSkills);

        normalizedSkills.forEach((skillId, weight) -> {
            SkillCoverage coverage = resolveSkillCoverage(
                    coverageScores.get(skillId),
                    coverageReasons.get(skillId)
            );

            courseChapterSkillMapper.insertChapterSkill(
                    chapterId,
                    skillId,
                    weight,
                    coverage.level().name(),
                    coverage.reason()
            );
        });

        courseSkillAggregationService.recalculateCourseSkills(chapter.getCourseId());
    }

    private Long resolveSkillId(Long chapterId, CourseSummarySkill skill) {
        if (skill == null) {
            return null;
        }

        String skillName = normalizeSkillName(skill.getSkillName());

        if (!isValidSkillName(skillName)) {
            log.info(
                    "AI 강의 요약 skill 제외. chapterId={}, skillName={}, sourceSkillName={}",
                    chapterId,
                    skill.getSkillName(),
                    skill.getSourceSkillName()
            );
            return null;
        }

        Long existingSkillId = courseChapterSkillMapper.findSkillIdByName(skillName);

        if (existingSkillId != null) {
            return existingSkillId;
        }

        if (!NEW_SKILL_YN.equalsIgnoreCase(normalizeNewSkillYn(skill.getNewSkillYn()))) {
            log.info(
                    "AI 강의 요약 skill 미매칭. chapterId={}, skillName={}, sourceSkillName={}, newSkillYn={}",
                    chapterId,
                    skillName,
                    skill.getSourceSkillName(),
                    skill.getNewSkillYn()
            );
            return null;
        }

        courseChapterSkillMapper.insertSkill(skillName, DEFAULT_SKILL_TYPE);
        Long newSkillId = courseChapterSkillMapper.findSkillIdByName(skillName);

        if (newSkillId == null) {
            throw new BaseException(
                    ErrorResponseCode.INTERNAL_SERVER_ERROR,
                    "신규 스킬 등록 후 조회에 실패했습니다. skillName=" + skillName
            );
        }

        log.info("AI 강의 요약 신규 skill 등록. skillName={}, skillId={}", skillName, newSkillId);

        return newSkillId;
    }

    private String normalizeSkillName(String skillName) {
        if (!StringUtils.hasText(skillName)) {
            return null;
        }

        return skillName.trim();
    }

    private boolean isValidSkillName(String skillName) {
        return StringUtils.hasText(skillName)
                && skillName.length() >= MIN_SKILL_NAME_LENGTH
                && skillName.length() <= MAX_SKILL_NAME_LENGTH;
    }

    private String normalizeNewSkillYn(String newSkillYn) {
        if (!StringUtils.hasText(newSkillYn)) {
            return EXISTING_SKILL_YN;
        }

        return newSkillYn.trim();
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

    private SkillCoverage resolveSkillCoverage(
            Map<CourseSkillCoverageLevel, BigDecimal> scores,
            Map<CourseSkillCoverageLevel, CoverageReasonCandidate> reasons
    ) {
        CourseSkillCoverageLevel selectedLevel = CourseSkillCoverageLevel.BASIC;
        BigDecimal selectedScore = BigDecimal.ZERO;

        if (scores != null) {
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
        }

        String reason = null;

        if (reasons != null && reasons.get(selectedLevel) != null) {
            reason = reasons.get(selectedLevel).reason();
        }

        if (!StringUtils.hasText(reason)) {
            reason = selectedLevel.getDefaultReason();
        }

        return new SkillCoverage(selectedLevel, reason);
    }

    private CourseChapter findChapter(Long chapterId) {
        if (chapterId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "chapterId는 필수입니다.");
        }

        CourseChapter chapter = courseChapterMapper.findById(chapterId);

        if (chapter == null) {
            throw new BaseException(
                    ErrorResponseCode.NOT_FOUND_ENDPOINT,
                    "존재하지 않는 챕터입니다. chapterId=" + chapterId
            );
        }

        return chapter;
    }

    private record SkillCoverage(CourseSkillCoverageLevel level, String reason) {
    }

    private record CoverageReasonCandidate(BigDecimal weight, String reason) {
    }
}
