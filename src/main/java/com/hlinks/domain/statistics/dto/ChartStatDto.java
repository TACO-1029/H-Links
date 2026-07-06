package com.hlinks.domain.statistics.dto;

import java.util.List;

public record ChartStatDto(
        String id,
        String title,
        String caption,
        String type,
        String unit,
        List<ChartSeriesDto> series
) {
}
