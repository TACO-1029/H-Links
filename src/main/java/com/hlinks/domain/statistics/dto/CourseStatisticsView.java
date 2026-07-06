package com.hlinks.domain.statistics.dto;

import java.util.List;

public record CourseStatisticsView(
        String title,
        String description,
        List<StatisticsBlockDto> kpis,
        List<StatisticsSectionDto> sections
) {
}
