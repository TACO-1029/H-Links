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

    // 4. 비즈니스 로직 및 UI 편의용 가상 메서드
    /**
     * Thymeleaf에서 th:if="${course.online}" 형태로
     * 온라인/오프라인 화면 분기를 매우 깔끔하게 처리할 수 있도록 돕는 유틸 메서드입니다.
     */
    public boolean isOnline() {
        return "ONLINE".equals(this.courseType);
    }
}