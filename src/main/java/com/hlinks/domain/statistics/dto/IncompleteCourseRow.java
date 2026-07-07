package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record IncompleteCourseRow(
        int rank,
        String courseTitle,
        String courseTypeName,
        String badgeTone,
        BigDecimal incompleteCount,
        BigDecimal completionRate
) {
}
