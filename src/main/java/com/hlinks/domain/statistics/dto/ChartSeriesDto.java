package com.hlinks.domain.statistics.dto;

import java.util.List;

public record ChartSeriesDto(
        String name,
        List<ChartPointDto> points
) {
}
