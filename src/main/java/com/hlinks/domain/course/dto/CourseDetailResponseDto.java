package com.hlinks.domain.course.dto;

import lombok.*;

import java.time.LocalDate;
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
}
