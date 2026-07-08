package com.hlinks.domain.hr.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AdminDashboardFilter(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        String courseType,
        String learningStatus,
        String completionStatus,
        String keyword,
        Long departmentId
) {

    public AdminDashboardFilter {
        LocalDate today = LocalDate.now();
        endDate = endDate != null ? endDate : today;
        startDate = startDate != null ? startDate : endDate.minusMonths(3).plusDays(1);

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        courseType = hasText(courseType) ? courseType.trim() : "all";
        learningStatus = hasText(learningStatus) ? learningStatus.trim() : "all";
        completionStatus = hasText(completionStatus) ? completionStatus.trim() : "all";
        keyword = hasText(keyword) ? keyword.trim() : "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
