package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record CourseStatusMonthlyRow(
        String monthLabel,
        String statusLabel,
        Integer statusOrder,
        BigDecimal statusCount
) {
}
