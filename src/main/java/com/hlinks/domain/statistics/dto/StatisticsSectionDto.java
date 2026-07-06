package com.hlinks.domain.statistics.dto;

import java.util.List;

public record StatisticsSectionDto(
        String title,
        List<StatisticsBlockDto> blocks
) {
}
