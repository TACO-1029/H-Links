package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
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

    // 4. 비즈니스 로직 및 UI 편의용 가상 메서드
    /**
     * Thymeleaf에서 th:if="${course.online}" 형태로
     * 온라인/오프라인 화면 분기를 매우 깔끔하게 처리할 수 있도록 돕는 유틸 메서드입니다.
     */
    public boolean isOnline() {
        return "ONLINE".equals(this.courseType);
    }
}