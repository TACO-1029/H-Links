package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record DepartmentCourseRankRow(
        String departmentName,
        int rank,
        String courseTitle,
        BigDecimal value
) {
}
