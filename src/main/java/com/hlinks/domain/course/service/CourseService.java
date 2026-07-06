package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.*;
import com.hlinks.domain.course.exception.CourseErrorCode;
import com.hlinks.domain.course.mapper.CourseApplicationMapper;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.domain.course.type.ApplicationType;
import com.hlinks.domain.course.type.CourseStatus;
import com.hlinks.domain.course.type.CourseType;
import com.hlinks.domain.course.type.LearningStatus;
import com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.SliceResponse;
import com.hlinks.global.response.code.ErrorResponseCode;
import com.hlinks.global.type.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseMapper courseMapper;

    // ========================================================
    // [이슈 #44] 마이페이지 신청 내역 관리를 위한 Mapper 주입 추가
    // ========================================================
    private final CourseApplicationMapper courseApplicationMapper;

    public List<CourseListResponseDto> getCourseList(String categoryType) {
        return getCourseList(categoryType, List.of(), List.of(), "latest");
    }

    public List<CourseListResponseDto> getCourseList(String categoryType, List<String> courseTypes, List<Long> skillIds) {
        return getCourseList(categoryType, courseTypes, skillIds, "latest");
    }

    public List<CourseListResponseDto> getCourseList(String categoryType, List<String> courseTypes, List<Long> skillIds, String sort) {
        log.info("강의 목록 조회 요청 - 카테고리 필터: {}", categoryType != null ? categoryType : "전체(ALL)");

        validateCategoryType(categoryType);

        String normalizedSort = "popular".equals(sort) ? "popular" : "latest";
        List<CourseListResponseDto> courses = courseMapper.findAllCourses(categoryType, courseTypes, skillIds, normalizedSort);

        // 목록 조회의 경우 데이터가 0건(Empty)인 것은 에러가 아니므로 정상 반환합니다.
        // 추후 '상세 조회(detail)' 시 조회된 결과가 null이면 여기서 BaseException(COURSE_NOT_FOUND)을 던지게 됩니다.

        log.info("강의 목록 조회 완료 - 조회된 강의 수: {}건", courses.size());
        return courses;
    }

    public SliceResponse<CourseListResponseDto> getCourseSlice(
            String categoryType,
            List<String> courseTypes,
            List<Long> skillIds,
            String keyword,
            String difficulty,
            Boolean availableOnly,
            String sort,
            int page,
            int size
    ) {
        validateCategoryType(categoryType);

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSliceSize(size);
        int limitPlusOne = normalizedSize + 1;
        int offset = normalizedPage * normalizedSize;
        String normalizedSort = "popular".equals(sort) ? "popular" : "latest";

        List<CourseListResponseDto> rows = courseMapper.findCourseSlice(
                categoryType,
                courseTypes != null ? courseTypes : List.of(),
                skillIds != null ? skillIds : List.of(),
                normalizeKeyword(keyword),
                difficulty,
                availableOnly,
                normalizedSort,
                offset,
                limitPlusOne
        );

        return toSliceResponse(rows, normalizedPage, normalizedSize);
    }

    public SliceResponse<CourseListResponseDto> getCareerHighCourseSlice(
            String keyword,
            Long categoryId,
            Boolean availableOnly,
            int page,
            int size
    ) {
        List<Long> skillIds = categoryId == null ? List.of() : List.of(categoryId);
        return getCourseSlice("CAREER_HIGH", List.of(), skillIds, keyword, null, availableOnly, "latest", page, size);
    }

    private void validateCategoryType(String categoryType) {
        if (categoryType != null && !categoryType.trim().isEmpty()
                && !categoryType.equals("CAREER_HIGH")
                && !categoryType.equals("CAREER_PATH")) {
            log.warn("유효하지 않은 카테고리 파라미터 접근: {}", categoryType);
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
        }
    }

    private int normalizeSliceSize(int size) {
        if (size <= 0) {
            return 12;
        }
        return Math.min(size, 50);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private SliceResponse<CourseListResponseDto> toSliceResponse(List<CourseListResponseDto> rows, int page, int size) {
        boolean hasNext = rows.size() > size;
        List<CourseListResponseDto> content = hasNext ? rows.subList(0, size) : rows;
        return SliceResponse.of(content, page, size, hasNext);
    }

    public List<SkillFilterGroupDto> getSkillFilterGroups() {
        List<SkillFilterOptionDto> options = courseMapper.findSkillFilterOptions();
        Map<Long, SkillFilterGroupDto.SkillFilterGroupDtoBuilder> builders = new LinkedHashMap<>();
        Map<Long, List<SkillFilterOptionDto>> groupedOptions = new LinkedHashMap<>();

        for (SkillFilterOptionDto option : options) {
            builders.putIfAbsent(option.getParentSkillId(), SkillFilterGroupDto.builder()
                    .skillId(option.getParentSkillId())
                    .skillName(option.getParentSkillName()));
            groupedOptions.computeIfAbsent(option.getParentSkillId(), key -> new ArrayList<>())
                    .add(option);
        }

        List<SkillFilterGroupDto> groups = new ArrayList<>();
        for (Map.Entry<Long, SkillFilterGroupDto.SkillFilterGroupDtoBuilder> entry : builders.entrySet()) {
            groups.add(entry.getValue()
                    .skills(groupedOptions.getOrDefault(entry.getKey(), List.of()))
                    .build());
        }
        return groups;
    }

    // ========================================================
    // [이슈 #31] 강의 상세 조회 비즈니스 로직 및 예외 검증
    // ========================================================
    /**
     * 특정 강의의 상세 정보를 조회합니다.
     * @param courseId 조회할 강의 고유 ID
     * @return 검증 및 바인딩이 완료된 상세 정보 DTO
     * @throws BaseException 존재하지 않는 강의 ID일 경우 COURSE_NOT_FOUND 예외 발생
     */
    public CourseDetailResponseDto getCourseDetail(Long courseId, Long userId) {
        log.info("강의 상세 조회 요청 - 강의 ID: {}", courseId);

        // 1. 파라미터 기본 검증 (ID가 null이거나 0 이하일 경우 예방)
        if (courseId == null || courseId <= 0) {
            log.warn("잘못된 형식의 강의 ID 접근 시도: {}", courseId);
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
        }

        // 2. Mapper를 통해 LEFT JOIN 완료된 단건 데이터 획득
        CourseDetailResponseDto courseDetail = courseMapper.findCourseDetailById(courseId);

        // 3. [방어 로직] 해당 ID의 강의가 DB에 존재하지 않을 경우 예외 처리
        if (courseDetail == null) {
            log.warn("존재하지 않는 강의 상세 페이지 접근 - 강의 ID: {}", courseId);
            throw new BaseException(ErrorResponseCode.COURSE_NOT_FOUND);
        }

        // 4. [일대다 바인딩] 커리큘럼 영역에 정렬되어 뿌려질 챕터(Chapter) 목록 주입
        List<ChapterResponseDto> chapters = courseMapper.findChaptersByCourseId(courseId);
        courseDetail.setChapters(chapters);

        // 5. [다대다 카테고리 변환 바인딩] 수정한 대분류 Skill 카테고리 리스트 주입
        List<String> skillNames = courseMapper.findSkillNamesByCourseId(courseId);
        courseDetail.setSkillNames(skillNames);

        // 6. [유저 컨텍스트 동적 주입] 로그인한 유저 ID가 유효한 경우 수강 신청 이력 매핑
        if (userId != null && userId > 0) {
            CourseDetailResponseDto appInfo = courseMapper.findApplicationInfo(courseId, userId);
            if (appInfo != null) {
                // 신청 이력이 존재하는 사원인 경우 데이터 덮어쓰기 주입 (WAITING, APPROVED, REJECTED 등)
                courseDetail.setApplicationStatus(appInfo.getApplicationStatus());
                courseDetail.setRejectReason(appInfo.getRejectReason());
                courseDetail.setLearningStatus(appInfo.getLearningStatus());
                courseDetail.setProgressRate(appInfo.getProgressRate());
                courseDetail.setNextLearningChapterId(resolveNextLearningChapterId(courseDetail, userId, courseId));
                Long courseLearningId = courseMapper.findCourseLearningId(userId, courseId);
                if (courseLearningId != null) {
                    courseDetail.setChapters(courseMapper.findChaptersByCourseIdWithLearning(courseId, courseLearningId));
                }
                log.info("사원 신청 정보 연동 성공 - 강좌 ID: {}, 상태값: {}", courseId, appInfo.getApplicationStatus());
            } else {
                // 신청 이력이 전혀 없는 사원인 경우 null 상태 유지 -> 화면단에서 '즉시 신청하기' 활성화
                log.info("사원 신청 정보 없음 (미신청 신규 사원) - 강좌 ID: {}", courseId);
            }
        }

        log.info("강의 대시보드 리치 데이터 모델 조립 최종 완료 - 타이틀: [{}], 챕터 수: {}건, 스킬 카테고리: {}",
                courseDetail.getCourseTitle(), chapters.size(), skillNames);

        return courseDetail;
    }

    private Long resolveNextLearningChapterId(CourseDetailResponseDto courseDetail, Long userId, Long courseId) {
        List<ChapterResponseDto> chapters = courseDetail.getChapters();
        if (chapters == null || chapters.isEmpty()) {
            return null;
        }

        if (courseDetail.isLearningInProgress()) {
            Long courseLearningId = courseMapper.findCourseLearningId(userId, courseId);
            Long latestChapterId = courseLearningId == null
                    ? null
                    : courseMapper.findLatestLearningChapterId(courseLearningId);
            if (latestChapterId != null) {
                return latestChapterId;
            }
        }

        return chapters.get(0).getChapterId();
    }

    @Transactional
    public CourseDetailResponseDto getOnlineChapterPage(Long courseId, Long chapterId, Long userId) {
        CourseDetailResponseDto courseDetail = getCourseDetail(courseId, userId);

        if (!courseDetail.isOnline() || !ApplicationStatus.APPROVED.name().equals(courseDetail.getApplicationStatus())) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN);
        }

        boolean chapterExists = courseDetail.getChapters().stream()
                .anyMatch(chapter -> chapter.getChapterId().equals(chapterId));
        if (!chapterExists) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "강의에 포함되지 않은 챕터입니다.");
        }

        Long courseLearningId = courseMapper.findCourseLearningId(userId, courseId);
        if (courseLearningId == null) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN);
        }

        courseMapper.insertChapterLearningStatus(
                courseLearningId,
                userId,
                courseId,
                chapterId,
                LearningStatus.NOT_STARTED.name()
        );
        courseDetail.setChapters(courseMapper.findChaptersByCourseIdWithLearning(courseId, courseLearningId));

        return courseDetail;
    }

    @Transactional
    public CourseApplyResponseDto applyCourse(Long courseId, Long userId) {
        log.info("강의 신청 요청 - courseId={}, userId={}", courseId, userId);

        CourseApplyTargetDto target = getApplyTargetForUpdateIfNeeded(courseId);
        validateCourseAvailability(target);
        validateUserCanApply(userId, courseId);

        ApplicationStatus applicationStatus = ApplicationStatus.APPROVED;
        Long applicationId = createCourseApplication(courseId, userId, applicationStatus.name());
        Long courseLearningId = initializeCourseLearningStatusIfApproved(
                applicationId,
                userId,
                courseId,
                applicationStatus
        );

        if (CourseType.OFFLINE.name().equals(target.getCourseType())) {
            int updated = courseMapper.increaseOfflineCurrentApplicantCount(courseId);

            if (updated == 0) {
                throw new BaseException(CourseErrorCode.COURSE_CAPACITY_FULL);
            }
        }

        log.info("강의 신청 완료 - courseId={}, userId={}, applicationId={}, courseLearningId={}",
                courseId, userId, applicationId, courseLearningId);

        return CourseApplyResponseDto.builder()
                .applicationId(applicationId)
                .courseLearningId(courseLearningId)
                .courseId(courseId)
                .applicationStatus(applicationStatus.name())
                .learningStatus(LearningStatus.NOT_STARTED.name())
                .build();
    }

    @Transactional
    public void cancelCourse(Long courseId, Long userId) {
        log.info("강의 신청 취소 요청 - courseId={}, userId={}", courseId, userId);

        CourseApplyTargetDto target = getApplyTargetForUpdateIfNeeded(courseId);
        CourseApplicationCancelTargetDto application =
                courseMapper.findActiveCourseApplicationForUpdate(userId, courseId);
        if (application == null) {
            throw new BaseException(CourseErrorCode.COURSE_APPLICATION_NOT_FOUND);
        }

        int canceledApplicationCount = courseMapper.cancelCourseApplication(
                application.getApplicationId(),
                ApplicationStatus.CANCELED.name()
        );
        if (canceledApplicationCount == 0) {
            throw new BaseException(CourseErrorCode.COURSE_APPLICATION_NOT_FOUND);
        }

        courseMapper.cancelCourseLearningStatus(
                application.getApplicationId(),
                LearningStatus.CANCELED.name()
        );

        if (CourseType.OFFLINE.name().equals(target.getCourseType())
                && ApplicationStatus.APPROVED.name().equals(application.getApplicationStatus())) {
            courseMapper.decreaseOfflineCurrentApplicantCount(courseId);
        }

        log.info("강의 신청 취소 완료 - courseId={}, userId={}, applicationId={}",
                courseId, userId, application.getApplicationId());
    }

    private CourseApplyTargetDto getApplyTargetForUpdateIfNeeded(Long courseId) {
        CourseApplyTargetDto target = courseMapper.findCourseApplyTarget(courseId);
        if (target == null) {
            throw new BaseException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        if (!CourseType.OFFLINE.name().equals(target.getCourseType())) {
            return target;
        }

        CourseApplyTargetDto lockedTarget = courseMapper.findOfflineCourseApplyTargetForUpdate(courseId);
        if (lockedTarget == null) {
            throw new BaseException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        return lockedTarget;
    }

    private void validateCourseAvailability(CourseApplyTargetDto target) {
        if (!CourseStatus.OPEN.name().equals(target.getCourseStatus())) {
            throw new BaseException(CourseErrorCode.COURSE_NOT_OPEN);
        }

        if (CourseType.ONLINE.name().equals(target.getCourseType())) {
            if (target.getIncompleteQuizBuildCount() == null || target.getIncompleteQuizBuildCount() > 0) {
                throw new BaseException(CourseErrorCode.COURSE_QUIZ_NOT_READY);
            }
            return;
        }

        if (!CourseType.OFFLINE.name().equals(target.getCourseType())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (target.getApplyStartDate() == null
                || target.getApplyEndDate() == null
                || now.isBefore(target.getApplyStartDate())
                || now.isAfter(target.getApplyEndDate())) {
            throw new BaseException(CourseErrorCode.COURSE_APPLY_PERIOD_NOT_OPEN);
        }

        Integer capacity = target.getCapacity();
        Integer currentApplicantCount = target.getCurrentApplicantCount();
        if (capacity != null && currentApplicantCount != null && currentApplicantCount >= capacity) {
            throw new BaseException(CourseErrorCode.COURSE_CAPACITY_FULL);
        }
    }

    private void validateUserCanApply(Long userId, Long courseId) {
        if (courseMapper.countActiveApplications(userId, courseId) > 0) {
            throw new BaseException(CourseErrorCode.COURSE_ALREADY_APPLIED);
        }
    }

    private Long createCourseApplication(Long courseId, Long userId, String applicationStatus) {
        Long applicationId = courseMapper.nextCourseApplicationId();
        courseMapper.insertCourseApplication(
                applicationId,
                userId,
                courseId,
                applicationStatus,
                ApplicationType.USER.name()
        );
        return applicationId;
    }

    private Long initializeCourseLearningStatusIfApproved(
            Long applicationId,
            Long userId,
            Long courseId,
            ApplicationStatus applicationStatus) {

        if (applicationStatus != ApplicationStatus.APPROVED) {
            return null;
        }

        return initializeCourseLearningStatus(applicationId, userId, courseId);
    }

    private Long initializeCourseLearningStatus(Long applicationId, Long userId, Long courseId) {
        Long courseLearningId = courseMapper.nextCourseLearningStatusId();
        courseMapper.insertCourseLearningStatus(
                courseLearningId,
                applicationId,
                userId,
                courseId,
                LearningStatus.NOT_STARTED.name()
        );
        return courseLearningId;
    }

    /**
     * [APP-001] 로그인한 유저의 신청 내역 목록을 최신순으로 조회합니다.
     */
    public List<CourseApplicationListResponseDto> getMyCourseApplicationList(Long userId) {
        log.info("마이페이지 내 신청 내역 목록 조회 요청 - 유저 ID: {}", userId);

        validateUserId(userId);

        return courseApplicationMapper.selectApplicationListByUserId(userId);
    }

    public int getMyInProgressCourseCount(Long userId) {
        validateUserId(userId);
        return courseMapper.countCourseLearningByStatus(userId, LearningStatus.IN_PROGRESS.name());
    }

    public int getMyCompletedCourseCount(Long userId) {
        validateUserId(userId);
        return courseMapper.countCourseLearningByStatus(userId, LearningStatus.COMPLETED.name());
    }

    /**
     * [이슈 #65] 마이페이지 내 수강 현황 대시보드 데이터를 조회합니다. (마이페이지/내 수강현황)
     */
    public MyCourseStatusResponseDto getMyCourseStatus(Long userId) {
        log.info("마이페이지 내 수강 현황 대시보드 데이터 조회 요청 - 유저 ID: {}", userId);
        validateUserId(userId);

        // 1. 테이블 내 데이터 존재 여부 판별 (이슈 #65 리팩터링)
        boolean hasLearnings = courseMapper.hasCourseLearnings(userId);
        boolean hasQuizAttempts = courseMapper.hasQuizAttempts(userId);
        boolean hasLearningLogs = courseMapper.hasLearningLogs(userId);

        // 2. 각 영역별 조건부 쿼리 실행 및 분기 처리
        int inProgressCount = hasLearnings ? getMyInProgressCourseCount(userId) : 0;
        int completedCount = hasLearnings ? getMyCompletedCourseCount(userId) : 0;
        int reinforcementCount = hasQuizAttempts ? courseMapper.countReinforcementCourses(userId) : 0;

        List<MyCourseStatusResponseDto.MyCourseDto> inProgressCourses = hasLearnings
                ? new ArrayList<>(courseMapper.selectMyCoursesWithProgress(userId))
                : List.of();

        if (hasLearnings && !inProgressCourses.isEmpty()) {
            inProgressCourses.sort((c1, c2) -> {
                int p1 = getStatusPriority(c1.getLearningStatus());
                int p2 = getStatusPriority(c2.getLearningStatus());
                return Integer.compare(p1, p2);
            });
        }

        MyCourseStatusResponseDto.RecentCompletedCourseDto recentCompletedCourse = (hasLearnings && completedCount > 0)
                ? courseMapper.selectRecentCompletedCourse(userId)
                : null;

        List<MyCourseStatusResponseDto.LearningActivityDto> learningActivities = hasLearningLogs
                ? courseMapper.selectLearningActivities(userId)
                : List.of();

        List<MyCourseStatusResponseDto.QuizWrongNoteDto> quizWrongNotes = hasQuizAttempts
                ? courseMapper.selectQuizWrongNotes(userId)
                : List.of();

        return MyCourseStatusResponseDto.builder()
                .inProgressCount(inProgressCount)
                .completedCount(completedCount)
                .reinforcementCount(reinforcementCount)
                .inProgressCourses(inProgressCourses)
                .recentCompletedCourse(recentCompletedCourse)
                .learningActivities(learningActivities)
                .quizWrongNotes(quizWrongNotes)
                .build();
    }

    private int getStatusPriority(String status) {
        if (status == null) return 4;
        switch (status) {
            case "NOT_STARTED": return 1;
            case "IN_PROGRESS": return 2;
            case "COMPLETED": return 3;
            default: return 4;
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
        }
    }
}
