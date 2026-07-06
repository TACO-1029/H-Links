package com.hlinks.domain.statistics.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum StatisticsPeriodUnit {
    DAY,
    WEEK,
    MONTH,
    QUARTER;

    public static StatisticsPeriodUnit from(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (days <= 7) {
            return DAY;
        }

        if (days <= 90) {
            return WEEK;
        }

        if (days <= 180) {
            return MONTH;
        }

        return QUARTER;
    }
}
