package com.hlinks.domain.statistics.dto;

import java.time.LocalDate;

public record SkillPopularityChangeQuery(
        LocalDate startDate,
        LocalDate endDate,
        LocalDate previousStartDate,
        LocalDate previousEndDate,
        String category,
        String courseType
) {
    public static SkillPopularityChangeQuery from(StatisticsFilter currentFilter, StatisticsFilter previousFilter) {
        return new SkillPopularityChangeQuery(
                currentFilter.startDate(),
                currentFilter.endDate(),
                previousFilter.startDate(),
                previousFilter.endDate(),
                currentFilter.category(),
                currentFilter.courseType()
        );
    }
}
