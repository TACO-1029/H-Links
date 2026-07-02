package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseApplicationCancelTargetDto;
import com.hlinks.domain.course.dto.CourseApplyResponseDto;
import com.hlinks.domain.course.dto.CourseApplyTargetDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.exception.CourseErrorCode;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import com.hlinks.global.type.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private static final String COURSE_TYPE_OFFLINE = "OFFLINE";
    private static final String STATUS_OPEN = "OPEN";
    private static final String APPLICATION_STATUS_APPROVED = ApplicationStatus.APPROVED.name();
    private static final String APPLICATION_STATUS_CANCELED = ApplicationStatus.CANCELED.name();
    private static final String APPLICATION_TYPE_USER = "USER";
    private static final String LEARNING_STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String LEARNING_STATUS_CANCELED = "CANCELED";

    private final CourseMapper courseMapper;

    public List<CourseListResponseDto> getCourseList(String categoryType) {
        log.info("강의 목록 조회 요청 - 카테고리 필터: {}", categoryType != null ? categoryType : "전체(ALL)");

        // ==========================================
        // [수정된 부분] BaseException 활용 방어 로직
        // ==========================================
        if (categoryType != null && !categoryType.trim().isEmpty()) {
            if (!categoryType.equals("CAREER_HIGH") && !categoryType.equals("CAREER_PATH")) {
                log.warn("유효하지 않은 카테고리 파라미터 접근: {}", categoryType);
                // 잘못된 파라미터가 들어오면 커스텀 예외를 던집니다.
                // (GlobalExceptionHandler가 이를 낚아채어 ErrorResponse로 프론트에 전달해 줍니다)
                throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
            }
        }

        List<CourseListResponseDto> courses = courseMapper.findAllCourses(categoryType);

        // 목록 조회의 경우 데이터가 0건(Empty)인 것은 에러가 아니므로 정상 반환합니다.
        // 추후 '상세 조회(detail)' 시 조회된 결과가 null이면 여기서 BaseException(COURSE_NOT_FOUND)을 던지게 됩니다.

        log.info("강의 목록 조회 완료 - 조회된 강의 수: {}건", courses.size());
        return courses;
    }

    @Transactional
    public CourseApplyResponseDto applyCourse(Long courseId, Long userId) {
        log.info("강의 신청 요청 - courseId={}, userId={}", courseId, userId);

        CourseApplyTargetDto target = getApplyTargetForUpdateIfNeeded(courseId);
        validateCourseAvailability(target);
        validateUserCanApply(userId, courseId);

        String applicationStatus = APPLICATION_STATUS_APPROVED;
        Long applicationId = createCourseApplication(courseId, userId, applicationStatus);
        Long courseLearningId = initializeCourseLearningStatusIfApproved(
                applicationId,
                userId,
                courseId,
                applicationStatus
        );

        if (COURSE_TYPE_OFFLINE.equals(target.getCourseType())) {
            courseMapper.increaseOfflineCurrentApplicantCount(courseId);
        }

        log.info("강의 신청 완료 - courseId={}, userId={}, applicationId={}, courseLearningId={}",
                courseId, userId, applicationId, courseLearningId);

        return CourseApplyResponseDto.builder()
                .applicationId(applicationId)
                .courseLearningId(courseLearningId)
                .courseId(courseId)
                .applicationStatus(applicationStatus)
                .learningStatus(LEARNING_STATUS_NOT_STARTED)
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
                APPLICATION_STATUS_CANCELED
        );
        if (canceledApplicationCount == 0) {
            throw new BaseException(CourseErrorCode.COURSE_APPLICATION_NOT_FOUND);
        }

        courseMapper.cancelCourseLearningStatus(
                application.getApplicationId(),
                LEARNING_STATUS_CANCELED
        );

        if (COURSE_TYPE_OFFLINE.equals(target.getCourseType())
                && APPLICATION_STATUS_APPROVED.equals(application.getApplicationStatus())) {
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

        if (!COURSE_TYPE_OFFLINE.equals(target.getCourseType())) {
            return target;
        }

        CourseApplyTargetDto lockedTarget = courseMapper.findOfflineCourseApplyTargetForUpdate(courseId);
        if (lockedTarget == null) {
            throw new BaseException(CourseErrorCode.COURSE_NOT_FOUND);
        }
        return lockedTarget;
    }

    private void validateCourseAvailability(CourseApplyTargetDto target) {
        if (!STATUS_OPEN.equals(target.getCourseStatus())) {
            throw new BaseException(CourseErrorCode.COURSE_NOT_OPEN);
        }

        if (!COURSE_TYPE_OFFLINE.equals(target.getCourseType())) {
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
                APPLICATION_TYPE_USER
        );
        return applicationId;
    }

    private Long initializeCourseLearningStatusIfApproved(
            Long applicationId,
            Long userId,
            Long courseId,
            String applicationStatus) {

        if (!APPLICATION_STATUS_APPROVED.equals(applicationStatus)) {
            return null;
        }

        return initializeCourseLearningStatus(applicationId, userId, courseId);
    }

    private Long initializeCourseLearningStatus(Long applicationId, Long userId, Long courseId) {
        Long courseLearningId = courseMapper.findCourseLearningId(userId, courseId);
        if (courseLearningId == null) {
            courseLearningId = courseMapper.nextCourseLearningStatusId();
            courseMapper.insertCourseLearningStatus(
                    courseLearningId,
                    applicationId,
                    userId,
                    courseId,
                    LEARNING_STATUS_NOT_STARTED
            );
        } else {
            courseMapper.resetCourseLearningStatus(
                    courseLearningId,
                    applicationId,
                    LEARNING_STATUS_NOT_STARTED
            );
        }
        return courseLearningId;
    }
}
