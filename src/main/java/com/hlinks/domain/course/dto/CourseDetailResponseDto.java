package com.hlinks.domain.course.dto;

import lombok.*;

import com.hlinks.domain.quiz.type.QuizBuildStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter // 서비스 레이어에서 연관 리스트(Chapters, Skills)를 매핑해 조립하기 위해 Setter 추가
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponseDto {

    private static final String COURSE_TYPE_ONLINE = "ONLINE";
    private static final String APPLICATION_STATUS_APPROVED = "APPROVED";
    private static final String APPLICATION_STATUS_WAITING = "WAITING";
    private static final String APPLICATION_STATUS_CANCELED = "CANCELED";
    private static final String APPLICATION_STATUS_REJECTED = "REJECTED";
    private static final String LEARNING_STATUS_NOT_STARTED = "NOT_STARTED";
    private static final String LEARNING_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String LEARNING_STATUS_COMPLETED = "COMPLETED";

    // 1. 공통 (COURSE 테이블)
    private Long courseId;
    private String categoryType;         // CAREER_HIGH, CAREER_PATH
    private String courseType;           // ONLINE, OFFLINE
    private String courseTitle;
    private String description;
    private String instructorName;
    private Integer totalDurationTime;
    private String thumbnailUrl;

    // 2. 온라인 특화 (ONLINE_COURSE 테이블)
    private String courseMaterialUrl;
    private String onlineStatus;         // 구조 구분을 위해 onlineStatus로 명명 (OPEN/CLOSED)

    // 3. 오프라인 특화 (OFFLINE_COURSE 테이블)
    private Integer capacity;
    private Integer currentApplicantCount;
    private String location;
    private String offlineStatus;        // 구조 구분을 위해 offlineStatus로 명명 (OPEN/CANCELED/HIDDEN)
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;

    // ========================================================
    // 🌟 [와이어프레임 100% 일치용 리치 객체 계층 레이어]
    // ========================================================

    /**
     * 와이어프레임 상단 메인 박스 내 '강의 skill 태그' 목록
     * (COURSE_SKILL - SKILL 테이블 조인 결과 수용)
     */
    private List<String> skillNames;

    /**
     * 와이어프레임 하단 독립 큰 박스 '커리큘럼' 내역
     * (COURSE_CHAPTER 테이블 일대다 바인딩)
     */
    private List<ChapterResponseDto> chapters;

    /**
     * 로그인한 사용자와 이 강좌의 '1:1 신청 현황 컨텍스트'
     * (COURSE_APPLICATION 테이블의 application_status 값: WAITING, APPROVED, REJECTED 등)
     * 신청 이력이 아예 없을 경우 null이 담기며, 화면단은 이에 따라 버튼 UI를 완전히 분기합니다.
     */
    private String applicationStatus;

    /**
     * 반려 사유 (필요시 반려 상태일 때 화면 알림용)
     */
    private String rejectReason;
    private String learningStatus;
    private Integer progressRate;
    private Long nextLearningChapterId;

    // 4. 비즈니스 로직 및 UI 편의용 가상 메서드
    /**
     * Thymeleaf에서 th:if="${course.online}" 형태로
     * 온라인/오프라인 화면 분기를 매우 깔끔하게 처리할 수 있도록 돕는 유틸 메서드입니다.
     */
    public boolean isOnline() {
        return COURSE_TYPE_ONLINE.equals(this.courseType);
    }

    public boolean isApplicationNotApplied() {
        return applicationStatus == null
                || APPLICATION_STATUS_CANCELED.equals(applicationStatus)
                || APPLICATION_STATUS_REJECTED.equals(applicationStatus);
    }

    public boolean isApplicationApproved() {
        return APPLICATION_STATUS_APPROVED.equals(applicationStatus);
    }

    public boolean isApplicationWaiting() {
        return APPLICATION_STATUS_WAITING.equals(applicationStatus);
    }

    public boolean isLearningCompleted() {
        return LEARNING_STATUS_COMPLETED.equals(learningStatus);
    }

    public boolean isLearningNotStarted() {
        return LEARNING_STATUS_NOT_STARTED.equals(learningStatus);
    }

    public boolean isLearningInProgress() {
        return LEARNING_STATUS_IN_PROGRESS.equals(learningStatus);
    }

    public boolean isCancelableApplication() {
        return isApplicationApproved() || isApplicationWaiting();
    }

    public boolean isOfflineApplyPeriodOpen() {
        if (isOnline() || applyStartDate == null || applyEndDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return !today.isBefore(applyStartDate) && !today.isAfter(applyEndDate);
    }

    public boolean isOnlineQuizBuildCompleted() {
        if (!isOnline()) {
            return false;
        }
        if (chapters == null || chapters.isEmpty()) {
            return true;
        }
        return chapters.stream()
                .allMatch(chapter -> QuizBuildStatus.COMPLETED.equals(chapter.getQuizBuildStatus()));
    }

    public String getOnlineApplyUnavailableMessage() {
        return "퀴즈 생성 완료 후 신청 가능";
    }

    public String getOfflineApplyUnavailableLabel() {
        if (isOnline() || isOfflineApplyPeriodOpen()) {
            return null;
        }

        LocalDate today = LocalDate.now();
        if (applyStartDate != null && today.isBefore(applyStartDate)) {
            return "접수 예정";
        }
        if (applyEndDate != null && today.isAfter(applyEndDate)) {
            return "접수기간 지남";
        }
        return "신청 불가";
    }

    public String getOfflineApplyUnavailableMessage() {
        if (isOnline() || isOfflineApplyPeriodOpen()) {
            return "오프라인 수강 신청";
        }

        LocalDate today = LocalDate.now();
        if (applyStartDate != null && today.isBefore(applyStartDate)) {
            return "접수 시작일 이후 신청할 수 있습니다.";
        }
        if (applyEndDate != null && today.isAfter(applyEndDate)) {
            return "접수기간이 지나 신청할 수 없습니다.";
        }
        return "현재 신청할 수 없는 강의입니다.";
    }

    public String getLearningCompletionLabel() {
        return isLearningCompleted() ? "완료" : "미완료";
    }

    public String getOnlineLearningActionLabel() {
        if (isLearningInProgress()) {
            return "이어서 학습하기";
        }
        if (isLearningCompleted()) {
            return "다시 학습하기";
        }
        return "학습하기";
    }

    public String getApplicationStatusLabel() {
        if (isApplicationApproved()) {
            return "신청 완료";
        }
        if (isApplicationWaiting()) {
            return "신청 승인 대기";
        }
        return "미신청";
    }

    public String getOfflineApplicationBadgeLabel() {
        if (isApplicationApproved()) {
            return "신청 승인 완료";
        }
        if (isApplicationWaiting()) {
            return "승인 대기";
        }
        return null;
    }

    public String getOfflineCourseStateLabel() {
        if (isLearningCompleted()) {
            return "수강 완료";
        }
        if (isLearningNotStarted()) {
            return "수강 예정";
        }
        if (isLearningInProgress()) {
            return "수강 중";
        }
        return "미신청";
    }

    public String getOfflineDateStatusLabel() {
        LocalDate today = LocalDate.now();

        if (courseStartDate == null) {
            return "수강 예정";
        }
        if (today.isBefore(courseStartDate)) {
            return "수강 예정";
        }
        if (courseEndDate == null || !today.isAfter(courseEndDate)) {
            return "수강 중";
        }
        return "수강 완료";
    }

    public String getOfflineCalendarTitle() {
        YearMonth calendarMonth = resolveOfflineCalendarMonth();
        return String.format("%d.%02d", calendarMonth.getYear(), calendarMonth.getMonthValue());
    }

    public List<List<OfflineCalendarDay>> getOfflineCalendarWeeks() {
        YearMonth calendarMonth = resolveOfflineCalendarMonth();
        LocalDate firstDay = calendarMonth.atDay(1);
        LocalDate lastDay = calendarMonth.atEndOfMonth();
        LocalDate cursor = firstDay.minusDays(firstDay.getDayOfWeek().getValue() % 7L);
        LocalDate end = lastDay.plusDays(6L - (lastDay.getDayOfWeek().getValue() % 7L));

        List<List<OfflineCalendarDay>> weeks = new ArrayList<>();
        List<OfflineCalendarDay> week = new ArrayList<>();
        while (!cursor.isAfter(end)) {
            week.add(new OfflineCalendarDay(
                    cursor.getDayOfMonth(),
                    cursor.getMonthValue() == calendarMonth.getMonthValue(),
                    cursor.getDayOfWeek().getValue() == 7,
                    isOfflineCoursePeriodDay(cursor),
                    courseStartDate != null && cursor.isEqual(courseStartDate),
                    courseEndDate != null && cursor.isEqual(courseEndDate)
            ));

            if (week.size() == 7) {
                weeks.add(week);
                week = new ArrayList<>();
            }
            cursor = cursor.plusDays(1);
        }
        return weeks;
    }

    private YearMonth resolveOfflineCalendarMonth() {
        LocalDate baseDate = courseStartDate != null
                ? courseStartDate
                : (applyStartDate != null ? applyStartDate : LocalDate.now());
        return YearMonth.from(baseDate);
    }

    private boolean isOfflineCoursePeriodDay(LocalDate date) {
        if (courseStartDate == null || courseEndDate == null) {
            return false;
        }
        return !date.isBefore(courseStartDate) && !date.isAfter(courseEndDate);
    }

    public String getCourseTypeLabel() {
        return isOnline() ? "온라인" : "오프라인";
    }

    public String getVirtualStatus() {
        LocalDate today = LocalDate.now();

        if (isOnline()) {
            return "CANCELED".equals(onlineStatus) ? "서비스 종료" : "상시 수강 가능";
        }

        if ("CANCELED".equals(offlineStatus)) {
            return "폐강";
        }
        if (applyStartDate != null && today.isBefore(applyStartDate)) {
            return "모집 대기";
        }
        if (applyStartDate != null && applyEndDate != null
                && !today.isBefore(applyStartDate) && !today.isAfter(applyEndDate)) {
            return "모집 중";
        }
        if (applyEndDate != null && courseStartDate != null
                && today.isAfter(applyEndDate) && today.isBefore(courseStartDate)) {
            return "모집 마감";
        }
        if (courseStartDate != null && courseEndDate != null
                && !today.isBefore(courseStartDate) && !today.isAfter(courseEndDate)) {
            return "강의 진행 중";
        }
        if (courseEndDate != null && today.isAfter(courseEndDate)) {
            return "강의 종료";
        }
        return "상태 불명";
    }

    @Getter
    @AllArgsConstructor
    public static class OfflineCalendarDay {
        private int day;
        private boolean currentMonth;
        private boolean sunday;
        private boolean coursePeriod;
        private boolean periodStart;
        private boolean periodEnd;
    }
}
