package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseApplyTargetDto;
import com.hlinks.domain.course.dto.CourseApplicationCancelTargetDto;
import com.hlinks.domain.course.dto.ChapterResponseDto;
import com.hlinks.domain.course.dto.CourseDetailResponseDto;
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

    int insertChapterLearningStatus(
            @Param("courseLearningId") Long courseLearningId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("status") String status
    );

    int startCourseLearning(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("status") String status
    );

    int startChapterLearning(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("status") String status
    );

    int increaseOfflineCurrentApplicantCount(@Param("courseId") Long courseId);

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

    /**
     * 1. 강의 상세 조회 (부모 + 온라인/오프라인 자식 테이블 LEFT JOIN)
     * @param courseId 조회할 강의 고유 ID
     * @return 가공된 상세 정보 DTO (존재하지 않으면 null 반환)
     */
    CourseDetailResponseDto findCourseDetailById(@Param("courseId") Long courseId);

    /**
     * [이슈 #31] 2. 특정 강의의 연관 챕터(커리큘럼) 목록 조회 (정렬 기준 반영)
     * @param courseId 강의 고유 ID
     * @return 정렬된 챕터 리스트
     */
    List<ChapterResponseDto> findChaptersByCourseId(@Param("courseId") Long courseId);

    Long findLatestLearningChapterId(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * [이슈 #31] 3. 특정 강의에서 다루는 핵심 기술 스킬명(Tags) 목록 조회
     * @param courseId 강의 고유 ID
     * @return 핵심 스킬명 리스트 (예: ["Spring", "Java", "SQL"])
     */
    List<String> findSkillNamesByCourseId(@Param("courseId") Long courseId);

    /**
     * [이슈 #31] 4. 특정 사용자의 해당 강의 수강 신청 정보 조회
     * @param courseId 강의 고유 ID
     * @param userId 현재 로그인한 사원 고유 ID
     * @return 신청 상태(Status) 및 반려 사유가 매핑된 DTO 객체 (이력이 없으면 null 반환)
     */
    CourseDetailResponseDto findApplicationInfo(@Param("courseId") Long courseId, @Param("userId") Long userId);
}
