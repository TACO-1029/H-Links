package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record ChartPointDto(
        String label,
        BigDecimal value,
        String displayValue
) {
}
