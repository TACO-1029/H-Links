package com.hlinks.domain.statistics.dto;

import java.time.LocalDate;
import java.util.List;

public record DepartmentGrowthQuery(
        LocalDate startDate,
        LocalDate endDate,
        Long departmentId,
        List<Long> departmentIds,
        Long positionId,
        StatisticsPeriodUnit periodUnit
) {
    public static DepartmentGrowthQuery from(StatisticsFilter filter) {
        return new DepartmentGrowthQuery(
                filter.startDate(),
                filter.endDate(),
                filter.departmentId(),
                filter.departmentIds(),
                filter.positionId(),
                StatisticsPeriodUnit.from(filter.startDate(), filter.endDate())
        );
    }
}
