package com.hlinks.domain.statistics.dto;

import java.time.LocalDate;
import java.util.List;

public record LearningPeriodQuery(
        LocalDate startDate,
        LocalDate endDate,
        Long departmentId,
        List<Long> departmentIds,
        Long positionId,
        String category,
        String courseType,
        StatisticsPeriodUnit periodUnit
) {
    public static LearningPeriodQuery from(StatisticsFilter filter) {
        return new LearningPeriodQuery(
                filter.startDate(),
                filter.endDate(),
                filter.departmentId(),
                filter.departmentIds(),
                filter.positionId(),
                filter.category(),
                filter.courseType(),
                StatisticsPeriodUnit.from(filter.startDate(), filter.endDate())
        );
    }
}
