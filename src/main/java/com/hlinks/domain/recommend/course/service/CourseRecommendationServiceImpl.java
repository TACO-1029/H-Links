package com.hlinks.domain.recommend.course.service;

import com.hlinks.domain.course.type.CourseSkillCoverageLevel;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationCandidateDto;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationResponse;
import com.hlinks.domain.recommend.course.dto.LevelTestSkillResultRequest;
import com.hlinks.domain.recommend.course.dto.RecommendedCourseDto;
import com.hlinks.domain.recommend.course.dto.RecommendedCourseSkillDto;
import com.hlinks.domain.recommend.course.mapper.CourseRecommendationMapper;
import com.hlinks.domain.recommend.course.type.LevelTestDifficulty;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseRecommendationServiceImpl implements CourseRecommendationService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;
    private static final double MIN_DEFICIENCY_SCORE = 10.0;
    private static final double PERFECT_COVERAGE_MATCH = 1.0;
    private static final double NEAR_COVERAGE_MATCH = 0.45;
    private static final double MIN_SKILL_WEIGHT_SCORE = 0.5;
    private static final double MIN_RECOMMENDABLE_SKILL_WEIGHT = 0.2;

    private final CourseRecommendationMapper courseRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public CourseRecommendationResponse recommendByLevelTest(CourseRecommendationRequest request) {
        validateRequest(request);

        Map<Long, SkillResult> skillResults = request.getResults().stream()
                .collect(Collectors.toMap(
                        LevelTestSkillResultRequest::getSkillId,
                        this::toSkillResult,
                        this::mergeSkillResult,
                        LinkedHashMap::new
                ));

        List<CourseRecommendationCandidateDto> candidates =
                courseRecommendationMapper.findRecommendationCandidates(skillResults.keySet().stream().toList());
        Map<Long, List<CourseRecommendationCandidateDto>> candidatesByCourse = candidates.stream()
                .collect(Collectors.groupingBy(
                        CourseRecommendationCandidateDto::getCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<RecommendedCourseDto> courses = candidatesByCourse.values().stream()
                .map(rows -> toRecommendedCourse(rows, skillResults))
                .filter(course -> course.getRecommendationScore() > 0)
                .sorted(Comparator
                        .comparingDouble(RecommendedCourseDto::getRecommendationScore).reversed()
                        .thenComparing(RecommendedCourseDto::getCourseId, Comparator.reverseOrder()))
                .limit(resolveLimit(request.getLimit()))
                .toList();

        return CourseRecommendationResponse.builder()
                .category(request.getCategory())
                .requestedSkillCount(skillResults.size())
                .courses(courses)
                .build();
    }

    private void validateRequest(CourseRecommendationRequest request) {
        if (request == null || !StringUtils.hasText(request.getCategory())) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "핵심 기술 분야는 필수입니다.");
        }

        if (request.getResults() == null || request.getResults().isEmpty()) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "레벨테스트 결과는 최소 1개 이상 필요합니다.");
        }

        if (request.getResults().stream().anyMatch(result -> result == null)) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "레벨테스트 결과 항목은 비어 있을 수 없습니다.");
        }
    }

    private SkillResult toSkillResult(LevelTestSkillResultRequest result) {
        if (result.getSkillId() == null || result.getScore() == null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "skillId와 score는 필수입니다.");
        }

        int score = Math.max(0, Math.min(100, result.getScore()));
        LevelTestDifficulty selectedDifficulty = LevelTestDifficulty.from(result.getSelectedDifficulty());
        CourseSkillCoverageLevel recommendedCoverageLevel = recommendCoverageLevel(selectedDifficulty, score);
        List<CourseSkillCoverageLevel> allowedCoverageLevels = resolveAllowedCoverageLevels(
                selectedDifficulty,
                score,
                recommendedCoverageLevel
        );
        double deficiencyScore = Math.max(MIN_DEFICIENCY_SCORE, 100.0 - score);

        return new SkillResult(
                result.getSkillId(),
                score,
                selectedDifficulty,
                recommendedCoverageLevel,
                allowedCoverageLevels,
                deficiencyScore
        );
    }

    private SkillResult mergeSkillResult(SkillResult previous, SkillResult current) {
        return previous.deficiencyScore() >= current.deficiencyScore() ? previous : current;
    }

    private CourseSkillCoverageLevel recommendCoverageLevel(LevelTestDifficulty selectedDifficulty, int score) {
        return switch (selectedDifficulty) {
            case LOW -> score >= 80
                    ? CourseSkillCoverageLevel.INTERMEDIATE
                    : CourseSkillCoverageLevel.BASIC;
            case MEDIUM -> {
                if (score >= 80) {
                    yield CourseSkillCoverageLevel.ADVANCED;
                }
                if (score >= 60) {
                    yield CourseSkillCoverageLevel.INTERMEDIATE;
                }
                yield CourseSkillCoverageLevel.BASIC;
            }
            case HIGH -> score >= 60
                    ? CourseSkillCoverageLevel.ADVANCED
                    : CourseSkillCoverageLevel.INTERMEDIATE;
        };
    }

    private List<CourseSkillCoverageLevel> resolveAllowedCoverageLevels(
            LevelTestDifficulty selectedDifficulty,
            int score,
            CourseSkillCoverageLevel recommendedCoverageLevel
    ) {
        return switch (selectedDifficulty) {
            case LOW -> score >= 80
                    ? List.of(CourseSkillCoverageLevel.INTERMEDIATE, CourseSkillCoverageLevel.BASIC)
                    : List.of(CourseSkillCoverageLevel.BASIC);
            case MEDIUM -> {
                if (score >= 80) {
                    yield List.of(CourseSkillCoverageLevel.ADVANCED, CourseSkillCoverageLevel.INTERMEDIATE);
                }
                if (score >= 60) {
                    yield List.of(CourseSkillCoverageLevel.INTERMEDIATE, CourseSkillCoverageLevel.BASIC);
                }
                yield List.of(CourseSkillCoverageLevel.BASIC, CourseSkillCoverageLevel.INTERMEDIATE);
            }
            case HIGH -> score >= 60
                    ? List.of(CourseSkillCoverageLevel.ADVANCED, CourseSkillCoverageLevel.INTERMEDIATE)
                    : List.of(CourseSkillCoverageLevel.INTERMEDIATE, CourseSkillCoverageLevel.ADVANCED);
        };
    }

    private RecommendedCourseDto toRecommendedCourse(
            List<CourseRecommendationCandidateDto> rows,
            Map<Long, SkillResult> skillResults
    ) {
        CourseRecommendationCandidateDto representative = rows.get(0);

        List<RecommendedCourseSkillDto> matchedSkills = rows.stream()
                .filter(row -> skillResults.containsKey(row.getSkillId()))
                .filter(row -> isRecommendableSkillWeight(row.getWeight()))
                .map(row -> toRecommendedCourseSkill(row, skillResults.get(row.getSkillId())))
                .filter(skill -> skill.getContributionScore() > 0)
                .sorted(Comparator.comparingDouble(RecommendedCourseSkillDto::getContributionScore).reversed())
                .toList();

        double recommendationScore = matchedSkills.stream()
                .mapToDouble(RecommendedCourseSkillDto::getContributionScore)
                .sum();

        return RecommendedCourseDto.builder()
                .courseId(representative.getCourseId())
                .categoryType(representative.getCategoryType())
                .courseType(representative.getCourseType())
                .courseTitle(representative.getCourseTitle())
                .instructorName(representative.getInstructorName())
                .thumbnailUrl(representative.getThumbnailUrl())
                .recommendationScore(round(recommendationScore))
                .reason(buildReason(matchedSkills))
                .matchedSkills(matchedSkills)
                .build();
    }

    private RecommendedCourseSkillDto toRecommendedCourseSkill(
            CourseRecommendationCandidateDto candidate,
            SkillResult skillResult
    ) {
        CourseSkillCoverageLevel courseCoverageLevel = CourseSkillCoverageLevel.from(candidate.getCoverageLevel());
        double coverageMatchScore = calculateCoverageMatchScore(
                skillResult.recommendedCoverageLevel(),
                courseCoverageLevel,
                skillResult.allowedCoverageLevels()
        );
        BigDecimal courseSkillWeight = candidate.getWeight() == null ? BigDecimal.ZERO : candidate.getWeight();
        double skillWeightScore = calculateSkillWeightScore(courseSkillWeight);
        double contributionScore = skillResult.deficiencyScore()
                * skillWeightScore
                * coverageMatchScore;

        return RecommendedCourseSkillDto.builder()
                .skillId(candidate.getSkillId())
                .skillName(candidate.getSkillName())
                .userScore(skillResult.score())
                .selectedDifficulty(skillResult.selectedDifficulty().name())
                .recommendedCoverageLevel(skillResult.recommendedCoverageLevel().name())
                .courseCoverageLevel(courseCoverageLevel.name())
                .courseSkillWeight(courseSkillWeight)
                .deficiencyScore(round(skillResult.deficiencyScore()))
                .coverageMatchScore(round(coverageMatchScore))
                .contributionScore(round(contributionScore))
                .build();
    }

    private double calculateCoverageMatchScore(
            CourseSkillCoverageLevel recommendedCoverageLevel,
            CourseSkillCoverageLevel courseCoverageLevel,
            List<CourseSkillCoverageLevel> allowedCoverageLevels
    ) {
        if (!allowedCoverageLevels.contains(courseCoverageLevel)) {
            return 0;
        }

        int distance = Math.abs(recommendedCoverageLevel.getRank() - courseCoverageLevel.getRank());

        if (distance == 0) {
            return PERFECT_COVERAGE_MATCH;
        }

        if (distance == 1) {
            return NEAR_COVERAGE_MATCH;
        }

        return 0;
    }

    private double calculateSkillWeightScore(BigDecimal courseSkillWeight) {
        double normalizedWeight = Math.max(0.0, Math.min(1.0, courseSkillWeight.doubleValue()));

        return MIN_SKILL_WEIGHT_SCORE + (normalizedWeight * (1.0 - MIN_SKILL_WEIGHT_SCORE));
    }

    private boolean isRecommendableSkillWeight(BigDecimal courseSkillWeight) {
        return courseSkillWeight != null
                && courseSkillWeight.doubleValue() >= MIN_RECOMMENDABLE_SKILL_WEIGHT;
    }

    private String buildReason(List<RecommendedCourseSkillDto> matchedSkills) {
        if (matchedSkills.isEmpty()) {
            return "레벨테스트 결과와 직접 연결된 스킬이 부족합니다.";
        }

        RecommendedCourseSkillDto primarySkill = matchedSkills.get(0);

        return "%s 점수가 %d점으로 측정되어 %s 수준 보완에 적합한 강의입니다."
                .formatted(
                        primarySkill.getSkillName(),
                        primarySkill.getUserScore(),
                        primarySkill.getRecommendedCoverageLevel()
                );
    }

    private int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }

        return Math.max(1, Math.min(limit, MAX_LIMIT));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private record SkillResult(
            Long skillId,
            int score,
            LevelTestDifficulty selectedDifficulty,
            CourseSkillCoverageLevel recommendedCoverageLevel,
            List<CourseSkillCoverageLevel> allowedCoverageLevels,
            double deficiencyScore
    ) {
    }
}
