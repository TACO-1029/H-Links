package com.hlinks.domain.statistics.dto;

import java.time.LocalDate;

public record CoursePeriodQuery(
        LocalDate startDate,
        LocalDate endDate,
        String category,
        String courseType,
        StatisticsPeriodUnit periodUnit
) {
    public static CoursePeriodQuery from(StatisticsFilter filter) {
        return new CoursePeriodQuery(
                filter.startDate(),
                filter.endDate(),
                filter.category(),
                filter.courseType(),
                StatisticsPeriodUnit.from(filter.startDate(), filter.endDate())
        );
    }
}
