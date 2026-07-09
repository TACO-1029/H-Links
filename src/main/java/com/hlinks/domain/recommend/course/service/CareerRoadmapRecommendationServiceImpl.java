package com.hlinks.domain.recommend.course.service;

import com.hlinks.domain.recommend.course.dto.CareerRoadmapResponse;
import com.hlinks.domain.recommend.course.dto.CareerRoadmapStepDto;
import com.hlinks.domain.recommend.course.dto.CoursePrerequisiteCandidateDto;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationResponse;
import com.hlinks.domain.recommend.course.dto.LevelTestSkillResultRequest;
import com.hlinks.domain.recommend.course.dto.RecommendedCourseDto;
import com.hlinks.domain.recommend.course.dto.RecommendedCourseSkillDto;
import com.hlinks.domain.recommend.course.mapper.CourseRecommendationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareerRoadmapRecommendationServiceImpl implements CareerRoadmapRecommendationService {

    private static final int ROADMAP_STEP_COUNT = 4;
    private static final int ROADMAP_CANDIDATE_LIMIT = 10;

    private final CourseRecommendationService courseRecommendationService;
    private final CourseRecommendationMapper courseRecommendationMapper;

    @Override
    @Transactional(readOnly = true)
    public CareerRoadmapResponse recommendRoadmapByLevelTest(CourseRecommendationRequest request) {
        CourseRecommendationResponse recommendation = courseRecommendationService.recommendByLevelTest(
                toRoadmapRecommendationRequest(request)
        );
        List<RecommendedCourseDto> recommendedCourses = recommendation.getCourses();

        if (CollectionUtils.isEmpty(recommendedCourses)) {
            return CareerRoadmapResponse.builder()
                    .category(recommendation.getCategory())
                    .requestedSkillCount(recommendation.getRequestedSkillCount())
                    .stepCount(0)
                    .summary("레벨테스트 결과와 연결된 추천 강의가 아직 없습니다.")
                    .steps(List.of())
                    .build();
        }

        Map<Long, List<CoursePrerequisiteCandidateDto>> prerequisitesByTargetCourse =
                findPrerequisitesByTargetCourse(recommendedCourses);
        List<CareerRoadmapStepDto> steps = buildRoadmapSteps(recommendedCourses, prerequisitesByTargetCourse);

        return CareerRoadmapResponse.builder()
                .category(recommendation.getCategory())
                .requestedSkillCount(recommendation.getRequestedSkillCount())
                .stepCount(steps.size())
                .summary(buildSummary(recommendation.getCategory(), steps))
                .steps(steps)
                .build();
    }

    private CourseRecommendationRequest toRoadmapRecommendationRequest(CourseRecommendationRequest request) {
        CourseRecommendationRequest roadmapRequest = new CourseRecommendationRequest();
        roadmapRequest.setCategory(request.getCategory());
        roadmapRequest.setResults(request.getResults());
        roadmapRequest.setLimit(resolveCandidateLimit(request.getLimit()));

        return roadmapRequest;
    }

    private int resolveCandidateLimit(Integer limit) {
        if (limit == null) {
            return ROADMAP_CANDIDATE_LIMIT;
        }

        return Math.max(ROADMAP_STEP_COUNT, Math.min(limit, ROADMAP_CANDIDATE_LIMIT));
    }

    private Map<Long, List<CoursePrerequisiteCandidateDto>> findPrerequisitesByTargetCourse(
            List<RecommendedCourseDto> recommendedCourses
    ) {
        List<Long> courseIds = recommendedCourses.stream()
                .map(RecommendedCourseDto::getCourseId)
                .toList();

        if (courseIds.isEmpty()) {
            return Map.of();
        }

        return courseRecommendationMapper.findPrerequisiteCandidates(courseIds).stream()
                .collect(Collectors.groupingBy(
                        CoursePrerequisiteCandidateDto::getTargetCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<CareerRoadmapStepDto> buildRoadmapSteps(
            List<RecommendedCourseDto> recommendedCourses,
            Map<Long, List<CoursePrerequisiteCandidateDto>> prerequisitesByTargetCourse
    ) {
        List<CareerRoadmapStepDto> steps = new ArrayList<>();
        Set<Long> usedCourseIds = new LinkedHashSet<>();

        for (RecommendedCourseDto recommendedCourse : recommendedCourses) {
            addPrerequisiteSteps(
                    steps,
                    usedCourseIds,
                    prerequisitesByTargetCourse.getOrDefault(recommendedCourse.getCourseId(), List.of()),
                    recommendedCourse
            );

            if (steps.size() >= ROADMAP_STEP_COUNT) {
                break;
            }

            addRecommendedStep(steps, usedCourseIds, recommendedCourse);

            if (steps.size() >= ROADMAP_STEP_COUNT) {
                break;
            }
        }

        return steps;
    }

    private void addPrerequisiteSteps(
            List<CareerRoadmapStepDto> steps,
            Set<Long> usedCourseIds,
            List<CoursePrerequisiteCandidateDto> prerequisiteRows,
            RecommendedCourseDto targetCourse
    ) {
        Map<Long, List<CoursePrerequisiteCandidateDto>> prerequisiteRowsByCourse = prerequisiteRows.stream()
                .collect(Collectors.groupingBy(
                        CoursePrerequisiteCandidateDto::getCourseId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<RecommendedCourseDto> prerequisiteCourses = prerequisiteRowsByCourse.values().stream()
                .map(this::toPrerequisiteCourse)
                .toList();

        for (RecommendedCourseDto prerequisiteCourse : prerequisiteCourses) {
            if (steps.size() >= ROADMAP_STEP_COUNT || !usedCourseIds.add(prerequisiteCourse.getCourseId())) {
                continue;
            }

            List<CoursePrerequisiteCandidateDto> rows = prerequisiteRowsByCourse.get(prerequisiteCourse.getCourseId());
            boolean required = rows.stream().anyMatch(CoursePrerequisiteCandidateDto::isRequired);
            String targetSkillName = resolveTargetSkillName(prerequisiteCourse);
            String targetCoverageLevel = resolveTargetCoverageLevel(prerequisiteCourse);

            steps.add(CareerRoadmapStepDto.builder()
                    .stepOrder(steps.size() + 1)
                    .title(buildPrerequisiteTitle(targetSkillName))
                    .description(buildPrerequisiteDescription(prerequisiteCourse, targetCourse, required))
                    .targetSkillName(targetSkillName)
                    .targetCoverageLevel(targetCoverageLevel)
                    .prerequisite(true)
                    .requiredPrerequisite(required)
                    .courses(List.of(prerequisiteCourse))
                    .build());
        }
    }

    private RecommendedCourseDto toPrerequisiteCourse(List<CoursePrerequisiteCandidateDto> rows) {
        CoursePrerequisiteCandidateDto representative = rows.get(0);
        List<RecommendedCourseSkillDto> skills = rows.stream()
                .filter(row -> row.getSkillId() != null)
                .map(this::toPrerequisiteSkill)
                .toList();

        return RecommendedCourseDto.builder()
                .courseId(representative.getCourseId())
                .categoryType(representative.getCategoryType())
                .courseType(representative.getCourseType())
                .courseTitle(representative.getCourseTitle())
                .instructorName(representative.getInstructorName())
                .thumbnailUrl(representative.getThumbnailUrl())
                .recommendationScore(0)
                .reason("다음 추천 강의를 이해하기 전에 먼저 들으면 좋은 선수 강의입니다.")
                .matchedSkills(skills)
                .build();
    }

    private RecommendedCourseSkillDto toPrerequisiteSkill(CoursePrerequisiteCandidateDto row) {
        BigDecimal weight = row.getWeight() == null ? BigDecimal.ZERO : row.getWeight();

        return RecommendedCourseSkillDto.builder()
                .skillId(row.getSkillId())
                .skillName(row.getSkillName())
                .userScore(0)
                .selectedDifficulty("")
                .recommendedCoverageLevel(row.getCoverageLevel())
                .courseCoverageLevel(row.getCoverageLevel())
                .courseSkillWeight(weight)
                .deficiencyScore(0)
                .coverageMatchScore(0)
                .contributionScore(0)
                .build();
    }

    private void addRecommendedStep(
            List<CareerRoadmapStepDto> steps,
            Set<Long> usedCourseIds,
            RecommendedCourseDto recommendedCourse
    ) {
        if (!usedCourseIds.add(recommendedCourse.getCourseId())) {
            return;
        }

        RecommendedCourseSkillDto primarySkill = resolvePrimarySkill(recommendedCourse);

        steps.add(CareerRoadmapStepDto.builder()
                .stepOrder(steps.size() + 1)
                .title(buildRecommendedTitle(primarySkill))
                .description(buildRecommendedDescription(recommendedCourse, primarySkill))
                .targetSkillName(resolveTargetSkillName(recommendedCourse))
                .targetCoverageLevel(resolveTargetCoverageLevel(recommendedCourse))
                .prerequisite(false)
                .requiredPrerequisite(false)
                .courses(List.of(recommendedCourse))
                .build());
    }

    private RecommendedCourseSkillDto resolvePrimarySkill(RecommendedCourseDto course) {
        if (CollectionUtils.isEmpty(course.getMatchedSkills())) {
            return null;
        }

        return course.getMatchedSkills().get(0);
    }

    private String resolveTargetSkillName(RecommendedCourseDto course) {
        RecommendedCourseSkillDto primarySkill = resolvePrimarySkill(course);

        if (primarySkill == null || primarySkill.getSkillName() == null) {
            return "핵심 역량";
        }

        return primarySkill.getSkillName();
    }

    private String resolveTargetCoverageLevel(RecommendedCourseDto course) {
        RecommendedCourseSkillDto primarySkill = resolvePrimarySkill(course);

        if (primarySkill == null || primarySkill.getCourseCoverageLevel() == null) {
            return "BASIC";
        }

        if (primarySkill.getRecommendedCoverageLevel() == null || primarySkill.getRecommendedCoverageLevel().isBlank()) {
            return primarySkill.getCourseCoverageLevel();
        }

        return primarySkill.getRecommendedCoverageLevel();
    }

    private String buildPrerequisiteTitle(String targetSkillName) {
        return "%s 선행 학습".formatted(targetSkillName);
    }

    private String buildPrerequisiteDescription(
            RecommendedCourseDto prerequisiteCourse,
            RecommendedCourseDto targetCourse,
            boolean required
    ) {
        String requiredText = required ? "필수 선수 강의" : "권장 선수 강의";

        return "%s인 %s을 먼저 학습하면 이후 %s 강의를 더 안정적으로 이해할 수 있습니다."
                .formatted(requiredText, prerequisiteCourse.getCourseTitle(), targetCourse.getCourseTitle());
    }

    private String buildRecommendedTitle(RecommendedCourseSkillDto primarySkill) {
        if (primarySkill == null) {
            return "추천 강의 학습";
        }

        return "%s %s 보완".formatted(
                resolveSkillName(primarySkill),
                resolveRecommendedCoverageLevel(primarySkill)
        );
    }

    private String buildRecommendedDescription(RecommendedCourseDto course, RecommendedCourseSkillDto primarySkill) {
        if (primarySkill == null) {
            return "%s 강의를 통해 현재 진단 결과와 연결된 역량을 보완합니다.".formatted(course.getCourseTitle());
        }

        return "%s 점수가 %d점으로 측정되어 %s 수준의 %s 학습을 추천합니다."
                .formatted(
                        resolveSkillName(primarySkill),
                        primarySkill.getUserScore(),
                        resolveRecommendedCoverageLevel(primarySkill),
                        course.getCourseTitle()
                );
    }

    private String resolveSkillName(RecommendedCourseSkillDto primarySkill) {
        if (primarySkill == null || primarySkill.getSkillName() == null || primarySkill.getSkillName().isBlank()) {
            return "핵심 역량";
        }

        return primarySkill.getSkillName();
    }

    private String resolveRecommendedCoverageLevel(RecommendedCourseSkillDto primarySkill) {
        if (primarySkill == null) {
            return "BASIC";
        }

        if (primarySkill.getRecommendedCoverageLevel() != null
                && !primarySkill.getRecommendedCoverageLevel().isBlank()) {
            return primarySkill.getRecommendedCoverageLevel();
        }

        if (primarySkill.getCourseCoverageLevel() != null
                && !primarySkill.getCourseCoverageLevel().isBlank()) {
            return primarySkill.getCourseCoverageLevel();
        }

        return "BASIC";
    }

    private String buildSummary(String category, List<CareerRoadmapStepDto> steps) {
        if (steps.isEmpty()) {
            return "현재 %s 분야에서 구성할 수 있는 로드맵이 없습니다.".formatted(category);
        }

        CareerRoadmapStepDto firstStep = steps.get(0);
        CareerRoadmapStepDto lastStep = steps.get(steps.size() - 1);

        return "%s 분야 진단 결과를 바탕으로 %s부터 시작해 %s까지 이어지는 %d단계 학습 로드맵입니다."
                .formatted(
                        category,
                        firstStep.getTargetSkillName(),
                        lastStep.getTargetSkillName(),
                        steps.size()
                );
    }
}
