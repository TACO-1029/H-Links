package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record StatisticsRankRow(
        int rank,
        String badgeText,
        String badgeTone,
        String label,
        BigDecimal value
) {
}
