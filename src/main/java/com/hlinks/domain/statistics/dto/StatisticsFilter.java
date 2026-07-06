package com.hlinks.domain.statistics.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record StatisticsFilter(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate,
        Long departmentId,
        List<Long> departmentIds,
        Long positionId,
        String category,
        String courseType
) {

    public StatisticsFilter {
        LocalDate today = LocalDate.now();
        endDate = endDate != null ? endDate : today;
        startDate = startDate != null ? startDate : endDate.minusMonths(3).plusDays(1);

        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        category = hasText(category) ? category.trim() : "all";
        courseType = hasText(courseType) ? courseType.trim() : "all";
        departmentIds = departmentIds == null ? List.of() : departmentIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .toList();
    }

    public boolean hasDepartmentIds() {
        return departmentIds != null && !departmentIds.isEmpty();
    }

    public StatisticsFilter withDepartmentIds(List<Long> nextDepartmentIds) {
        return new StatisticsFilter(
                startDate,
                endDate,
                departmentId,
                nextDepartmentIds,
                positionId,
                category,
                courseType
        );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
