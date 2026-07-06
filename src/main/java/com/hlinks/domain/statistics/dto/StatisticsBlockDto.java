package com.hlinks.domain.statistics.dto;

import java.util.List;

public record StatisticsBlockDto(
        int span,
        StatisticsBlockKind kind,
        KpiStatDto kpi,
        ChartStatDto chart,
        String rankTitle,
        String rankCaption,
        List<RankStatDto> ranks
) {

    public static StatisticsBlockDto kpi(int span, KpiStatDto kpi) {
        return new StatisticsBlockDto(span, StatisticsBlockKind.KPI, kpi, null, null, null, List.of());
    }

    public static StatisticsBlockDto chart(int span, ChartStatDto chart) {
        return new StatisticsBlockDto(span, StatisticsBlockKind.CHART, null, chart, null, null, List.of());
    }

    public static StatisticsBlockDto rank(int span, String title, String caption, List<RankStatDto> ranks) {
        return new StatisticsBlockDto(span, StatisticsBlockKind.RANK, null, null, title, caption, ranks);
    }

    public String spanClass() {
        int safeSpan = Math.max(1, Math.min(span, 4));
        return "span-" + safeSpan;
    }
}
