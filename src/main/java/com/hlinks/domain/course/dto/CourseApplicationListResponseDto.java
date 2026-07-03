package com.hlinks.domain.course.dto;

import com.hlinks.global.type.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseApplicationListResponseDto {

    // 1. 신청 기본 정보
    private Long applicationId;       // ID값들은 객체타입(Long) 권장
    private Long userId;
    private String applicationType;   // USER, ADMIN_ASSIGN
    private ApplicationStatus applicationStatus; // WAITING, APPROVED, REJECTED, CANCELED
    private String rejectReason;      // 반려 사유
    private LocalDate appliedAt;  // 시간까지 표출하기 위해 LocalDateTime 권장 (추후 리팩토링 여지)
    private LocalDate canceledAt; // 취소 일시
    private Long approvedBy;          // 승인자 ID (Null 가능)
    private LocalDate approvedAt; // 승인 일시

    // 2. 화면 출력을 위한 강의 정보 (COURSE 테이블 조인)
    private Long courseId;
    private String courseTitle;       // 강의명
    private String courseType;        // ONLINE, OFFLINE
    private String categoryType;      // CAREER_PATH, CAREER_HIGH
    private String instructorName;    // 강사명

    // 3. 오프라인 강의 필수 일정 정보 (OFFLINE_COURSE 테이블 조인)
    private String location;          // 강의 장소
    private LocalDate applyStartDate; // 신청 시작일
    private LocalDate applyEndDate;   // 신청 종료일
    private LocalDate courseStartDate;// 강의 시작일
    private LocalDate courseEndDate;  // 강의 종료일
}