package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record PopularCourseRow(
        int rank,
        String courseTitle,
        String categoryName,
        String courseTypeName,
        BigDecimal applicationCount,
        BigDecimal completionRate,
        BigDecimal averageProgressRate,
        BigDecimal quizCorrectRate
) {
}
