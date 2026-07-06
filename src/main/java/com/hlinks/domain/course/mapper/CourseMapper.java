package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseApplyTargetDto;
import com.hlinks.domain.course.dto.CourseApplicationCancelTargetDto;
import com.hlinks.domain.course.dto.ChapterResponseDto;
import com.hlinks.domain.course.dto.CourseDetailResponseDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.dto.OnlineChapterAccessTargetDto;
import com.hlinks.domain.course.entity.Course;
import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.dto.LearningProgressTargetDto;
import com.hlinks.domain.course.dto.CourseSkillAggregationRow;
import com.hlinks.domain.course.dto.SkillFilterOptionDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseMapper {

    int insertCourse(Course course);

    int updateCourseThumbnail(
            @Param("courseId") Long courseId,
            @Param("thumbnailUrl") String thumbnailUrl
    );

    int insertOnlineCourse(
            @Param("courseId") Long courseId,
            @Param("courseMaterialUrl") String courseMaterialUrl
    );

    int insertOfflineCourse(
            @Param("courseId") Long courseId,
            @Param("capacity") Integer capacity,
            @Param("location") String location,
            @Param("applyStartDate") LocalDate applyStartDate,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("courseStartDate") LocalDate courseStartDate,
            @Param("courseEndDate") LocalDate courseEndDate
    );

    int insertCourseChapter(CourseChapter chapter);

    int updateCourseChapterVideo(CourseChapter chapter);

    /**
     * 전체 강의 목록 조회 (HIDDEN 제외 및 상태 가공)
     * @param categoryType 선택적 필터링 (CAREER_HIGH, CAREER_PATH). null이거나 빈 값이면 전체 조회
     * @return 화면용 List DTO
     */
    List<CourseListResponseDto> findAllCourses(
            @Param("categoryType") String categoryType,
            @Param("courseTypes") List<String> courseTypes,
            @Param("skillIds") List<Long> skillIds,
            @Param("sort") String sort
    );

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

    int insertChapterLearningStatus(
            @Param("courseLearningId") Long courseLearningId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("status") String status
    );

    int startCourseLearning(
            @Param("courseLearningId") Long courseLearningId,
            @Param("status") String status
    );

    int startChapterLearning(
            @Param("courseLearningId") Long courseLearningId,
            @Param("chapterId") Long chapterId,
            @Param("status") String status
    );

    LearningProgressTargetDto findLearningProgressTarget(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId
    );

    int updateChapterLearningProgress(
            @Param("chapterLearningId") Long chapterLearningId,
            @Param("lastPlaySeconds") int lastPlaySeconds,
            @Param("maxPlaySeconds") int maxPlaySeconds,
            @Param("progressRate") int progressRate
    );

    int completeChapterLearning(
            @Param("chapterLearningId") Long chapterLearningId,
            @Param("lastPlaySeconds") int lastPlaySeconds,
            @Param("maxPlaySeconds") int maxPlaySeconds,
            @Param("progressRate") int progressRate,
            @Param("status") String status
    );

    int completeChapterLearningByQuizAttempt(
            @Param("chapterLearningId") Long chapterLearningId,
            @Param("quizAttemptId") Long quizAttemptId,
            @Param("status") String status
    );

    int updateCourseLearningProgress(
            @Param("courseLearningId") Long courseLearningId,
            @Param("courseId") Long courseId
    );

    int completeCourseLearningIfAllChaptersCompleted(
            @Param("courseLearningId") Long courseLearningId,
            @Param("courseId") Long courseId,
            @Param("status") String status
    );

    void insertLearningLog(
            @Param("courseLearningId") Long courseLearningId,
            @Param("chapterLearningId") Long chapterLearningId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("eventType") String eventType,
            @Param("playSeconds") int playSeconds,
            @Param("progressRate") int progressRate
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

    int countCourseLearningByStatus(
            @Param("userId") Long userId,
            @Param("status") String status
    );

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

    List<ChapterResponseDto> findChaptersByCourseIdWithLearning(
            @Param("courseId") Long courseId,
            @Param("courseLearningId") Long courseLearningId
    );

    Long findLatestLearningChapterId(@Param("courseLearningId") Long courseLearningId);

    OnlineChapterAccessTargetDto findOnlineChapterAccessTarget(
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId,
            @Param("userId") Long userId
    );

    /**
     * [이슈 #31] 3. 특정 강의에서 다루는 핵심 기술 스킬명(Tags) 목록 조회
     * @param courseId 강의 고유 ID
     * @return 핵심 스킬명 리스트 (예: ["Spring", "Java", "SQL"])
     */
    List<String> findSkillNamesByCourseId(@Param("courseId") Long courseId);

    List<SkillFilterOptionDto> findSkillFilterOptions();

    List<CourseSkillAggregationRow> findChapterSkillsByCourseId(@Param("courseId") Long courseId);

    int deleteCourseSkillsByCourseId(@Param("courseId") Long courseId);

    int insertCourseSkill(
            @Param("courseId") Long courseId,
            @Param("skillId") Long skillId,
            @Param("weight") BigDecimal weight,
            @Param("coverageLevel") String coverageLevel,
            @Param("coverageReason") String coverageReason
    );

    List<Long> findCourseIdsHavingChapterSkills();

    /**
     * [이슈 #31] 4. 특정 사용자의 해당 강의 수강 신청 정보 조회
     * @param courseId 강의 고유 ID
     * @param userId 현재 로그인한 사원 고유 ID
     * @return 신청 상태(Status) 및 반려 사유가 매핑된 DTO 객체 (이력이 없으면 null 반환)
     */
    CourseDetailResponseDto findApplicationInfo(@Param("courseId") Long courseId, @Param("userId") Long userId);

    // ========================================================
    // [이슈 #65] 마이페이지/내 수강현황 관련 추가 메서드들
    // ========================================================

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 사용자별 신청/수강 코스 정보 및 진도율 조회
     */
    List<com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto.MyCourseDto> selectMyCoursesWithProgress(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 최근 수료한 강의 단건 조회
     */
    com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto.RecentCompletedCourseDto selectRecentCompletedCourse(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 실시간 학습 활동 로그 조회
     */
    List<com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto.LearningActivityDto> selectLearningActivities(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 오답노트 문항 및 해설 조회
     */
    List<com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto.QuizWrongNoteDto> selectQuizWrongNotes(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 퀴즈 평균 점수가 80점 미만인 보완 필요 코스 수 카운트
     */
    int countReinforcementCourses(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 사용자 학습 이력이 존재하는지 확인
     */
    boolean hasCourseLearnings(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 사용자 퀴즈 응시 이력이 존재하는지 확인
     */
    boolean hasQuizAttempts(@Param("userId") Long userId);

    /**
     * [이슈 #65] 마이페이지/내 수강현황 - 사용자 학습 로그가 존재하는지 확인
     */
    boolean hasLearningLogs(@Param("userId") Long userId);
}

