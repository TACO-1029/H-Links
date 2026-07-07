package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record ChartPointDto(
        String label,
        BigDecimal value,
        String displayValue,
        Long participantCount
) {
    public ChartPointDto(String label, BigDecimal value, String displayValue) {
        this(label, value, displayValue, null);
    }
}
