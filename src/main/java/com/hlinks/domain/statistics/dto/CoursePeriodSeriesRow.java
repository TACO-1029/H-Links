package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record CoursePeriodSeriesRow(
        String periodLabel,
        String seriesName,
        BigDecimal value
) {
}
