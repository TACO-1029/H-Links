package com.hlinks.domain.statistics.dto;

import java.util.List;

public record LearningStatisticsView(
        String title,
        String description,
        List<StatisticsBlockDto> kpis,
        List<StatisticsSectionDto> sections
) {
}
