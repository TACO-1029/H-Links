package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record StatisticsPointRow(
        String label,
        BigDecimal value
) {
}
