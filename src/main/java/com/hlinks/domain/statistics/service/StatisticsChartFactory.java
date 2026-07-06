package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.ChartPointDto;
import com.hlinks.domain.statistics.dto.ChartSeriesDto;
import com.hlinks.domain.statistics.dto.ChartStatDto;
import com.hlinks.domain.statistics.dto.RankStatDto;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import com.hlinks.domain.statistics.dto.StatisticsRankRow;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

final class StatisticsChartFactory {

    private StatisticsChartFactory() {
    }

    static ChartStatDto chart(
            String id,
            String title,
            String caption,
            String type,
            String unit,
            String seriesName,
            List<StatisticsPointRow> rows
    ) {
        return new ChartStatDto(
                id,
                title,
                caption,
                type,
                unit,
                List.of(new ChartSeriesDto(seriesName, toPoints(rows, unit)))
        );
    }

    static List<RankStatDto> ranks(List<StatisticsRankRow> rows, String unit) {
        return rows.stream()
                .map(row -> new RankStatDto(
                        row.rank(),
                        row.badgeText(),
                        row.badgeTone(),
                        row.label(),
                        display(row.value(), unit)
                ))
                .toList();
    }

    static String display(BigDecimal value, String unit) {
        BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;
        BigDecimal normalized = safeValue.stripTrailingZeros();
        String formatted = NumberFormat.getNumberInstance(Locale.KOREA).format(normalized);
        return formatted + (unit == null ? "" : unit);
    }

    static String displayPercent(BigDecimal value) {
        BigDecimal safeValue = value != null ? value : BigDecimal.ZERO;
        return safeValue.setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "%";
    }

    private static List<ChartPointDto> toPoints(List<StatisticsPointRow> rows, String unit) {
        return rows.stream()
                .map(row -> new ChartPointDto(row.label(), safe(row.value()), display(row.value(), unit)))
                .toList();
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
