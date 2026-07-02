package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseApplyTargetDto;
import com.hlinks.domain.course.dto.CourseApplicationCancelTargetDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {

    /**
     * 전체 강의 목록 조회 (HIDDEN 제외 및 상태 가공)
     * @param categoryType 선택적 필터링 (CAREER_HIGH, CAREER_PATH). null이거나 빈 값이면 전체 조회
     * @return 화면용 List DTO
     */
    List<CourseListResponseDto> findAllCourses(@Param("categoryType") String categoryType);

    CourseApplyTargetDto findCourseApplyTarget(@Param("courseId") Long courseId);

    CourseApplyTargetDto findOfflineCourseApplyTargetForUpdate(@Param("courseId") Long courseId);

    int countActiveApplications(@Param("userId") Long userId, @Param("courseId") Long courseId);

    Long nextCourseApplicationId();

    void insertCourseApplication(
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("applicationStatus") String applicationStatus,
            @Param("applicationType") String applicationType
    );

    Long findCourseLearningId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    Long nextCourseLearningStatusId();

    void insertCourseLearningStatus(
            @Param("courseLearningId") Long courseLearningId,
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("status") String status
    );

    void resetCourseLearningStatus(
            @Param("courseLearningId") Long courseLearningId,
            @Param("applicationId") Long applicationId,
            @Param("status") String status
    );

    void increaseOfflineCurrentApplicantCount(@Param("courseId") Long courseId);

    CourseApplicationCancelTargetDto findActiveCourseApplicationForUpdate(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId
    );

    int cancelCourseApplication(
            @Param("applicationId") Long applicationId,
            @Param("applicationStatus") String applicationStatus
    );

    int cancelCourseLearningStatus(
            @Param("applicationId") Long applicationId,
            @Param("status") String status
    );

    int decreaseOfflineCurrentApplicantCount(@Param("courseId") Long courseId);

}
