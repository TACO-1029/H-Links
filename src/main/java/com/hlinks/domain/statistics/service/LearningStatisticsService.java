package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.ChartPointDto;
import com.hlinks.domain.statistics.dto.ChartSeriesDto;
import com.hlinks.domain.statistics.dto.ChartStatDto;
import com.hlinks.domain.statistics.dto.KpiStatDto;
import com.hlinks.domain.statistics.dto.LearningKpiStats;
import com.hlinks.domain.statistics.dto.LearningStatisticsView;
import com.hlinks.domain.statistics.dto.StatisticsBlockDto;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import com.hlinks.domain.statistics.dto.StatisticsScope;
import com.hlinks.domain.statistics.dto.StatisticsSectionDto;
import com.hlinks.domain.statistics.mapper.LearningStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LearningStatisticsService {

    private final LearningStatisticsMapper learningStatisticsMapper;

    public LearningStatisticsView getLearningStatistics(StatisticsFilter filter, StatisticsScope scope) {
        LearningKpiStats currentKpis = learningStatisticsMapper.selectLearningKpis(filter);
        LearningKpiStats previousKpis = learningStatisticsMapper.selectLearningKpis(previousPeriodFilter(filter));

        return new LearningStatisticsView(
                "학습 통계",
                "선택한 기간, 부서, 직급의 학습 현황을 종합적으로 분석합니다.",
                List.of(
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-people-fill", "현재 수강 중 인원", number(currentKpis.activeLearners(), "명"), ratioHint(currentKpis.activeLearners(), currentKpis.totalLearners()), "")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-mortarboard-fill", "전체 수료율", percent(currentKpis.completionRate()), previousPeriodHint(currentKpis.completionRate(), previousKpis.completionRate()), "blue")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-pie-chart-fill", "평균 진도율", percent(currentKpis.averageProgressRate()), previousPeriodHint(currentKpis.averageProgressRate(), previousKpis.averageProgressRate()), "cyan")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-bullseye", "평균 퀴즈 정답률", percent(currentKpis.quizCorrectRate()), previousPeriodHint(currentKpis.quizCorrectRate(), previousKpis.quizCorrectRate()), "purple"))
                ),
                List.of(
                        new StatisticsSectionDto("학습 활동", List.of(
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-hourly-pattern", "시간대별 수강 패턴", "수강자 수", "bar", "명", "수강자 수", learningStatisticsMapper.selectHourlyLearningPattern(filter))),
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-weekday-pattern", "요일별 학습 패턴", "수강자 수", "bar", "명", "수강자 수", learningStatisticsMapper.selectWeekdayLearningPattern(filter))),
                                StatisticsBlockDto.chart(4, StatisticsChartFactory.chart("learning-weekly-hours", "주간 학습량 추이", "평균 학습 시간", "line", "h", "평균 학습 시간", learningStatisticsMapper.selectWeeklyLearningHours(filter)))
                        )),
                        new StatisticsSectionDto("참여 분석", List.of(
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-participation-funnel", "전직원 학습 참여 퍼널", "전체 임직원 대비 단계별 참여율", "funnel", "%", "참여율", learningStatisticsMapper.selectLearningParticipationFunnel(filter))),
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-kcy-participation", "KCY 개발성향별 학습 참여율", "선택 기간 내 학습 참여자 기준", "horizontalBar", "%", "참여율", learningStatisticsMapper.selectLearningParticipationRateByKcy(filter)))
                        )),
                        new StatisticsSectionDto("성장 분석", List.of(
                                StatisticsBlockDto.chart(1, averageCompetencyWithCompanyAverage(filter)),
                                StatisticsBlockDto.chart(3, competencyGrowthFactors(filter))
                        ))
                )
        );
    }

    private ChartStatDto averageCompetencyWithCompanyAverage(StatisticsFilter filter) {
        List<StatisticsPointRow> selectedRows = learningStatisticsMapper.selectAverageCompetencyScores(filter);
        boolean hasDepartmentFilter = hasDepartmentFilter(filter);
        List<StatisticsPointRow> companyRows = hasDepartmentFilter
                ? learningStatisticsMapper.selectCompanyAverageCompetencyScores(filter)
                : List.of();

        Set<String> labels = new LinkedHashSet<>();
        selectedRows.forEach(row -> labels.add(row.label()));
        companyRows.forEach(row -> labels.add(row.label()));

        List<ChartSeriesDto> series = List.of(
                new ChartSeriesDto("선택 조건 평균", toAlignedPoints(labels, selectedRows)),
                new ChartSeriesDto("전직원 평균", toAlignedPoints(labels, companyRows))
        );

        if (!hasDepartmentFilter) {
            series = series.subList(0, 1);
        }

        return new ChartStatDto(
                "learning-average-skill",
                "평균 역량 점수",
                "선택 조건 평균과 전직원 평균 비교",
                "radar",
                "점",
                series
        );
    }

    private boolean hasDepartmentFilter(StatisticsFilter filter) {
        return filter.departmentId() != null || filter.hasDepartmentIds();
    }

    private ChartStatDto competencyGrowthFactors(StatisticsFilter filter) {
        List<StatisticsPointRow> cumulativeGrowthRows = learningStatisticsMapper.selectMonthlyCompetencyGrowthInPeriod(filter);
        List<StatisticsPointRow> completedCourseRows = learningStatisticsMapper.selectMonthlyCompletedCourseCounts(filter);

        Set<String> labels = new LinkedHashSet<>();
        cumulativeGrowthRows.forEach(row -> labels.add(row.label()));
        completedCourseRows.forEach(row -> labels.add(row.label()));

        return new ChartStatDto(
                "learning-competency-growth-in-period",
                "기간 내 역량 성장 요인",
                "월별 역량 증가폭과 수료 강의 수",
                "growthFactors",
                "",
                List.of(
                        new ChartSeriesDto("역량 증가폭", toMonthlyIncreasePoints(labels, cumulativeGrowthRows)),
                        new ChartSeriesDto("수료 강의 수", toAlignedPoints(labels, completedCourseRows, "건"))
                )
        );
    }

    private List<ChartPointDto> toMonthlyIncreasePoints(Set<String> labels, List<StatisticsPointRow> rows) {
        Map<String, BigDecimal> valuesByLabel = new LinkedHashMap<>();
        rows.forEach(row -> valuesByLabel.put(row.label(), safe(row.value())));

        BigDecimal[] previous = {BigDecimal.ZERO};
        return labels.stream()
                .map(label -> {
                    BigDecimal current = valuesByLabel.getOrDefault(label, previous[0]);
                    BigDecimal increase = current.subtract(previous[0]).setScale(1, RoundingMode.HALF_UP);
                    previous[0] = current;
                    return new ChartPointDto(label, increase, number(increase, "%p"));
                })
                .toList();
    }

    private List<ChartPointDto> toAlignedPoints(Set<String> labels, List<StatisticsPointRow> rows) {
        return toAlignedPoints(labels, rows, "점");
    }

    private List<ChartPointDto> toAlignedPoints(Set<String> labels, List<StatisticsPointRow> rows, String unit) {
        Map<String, BigDecimal> valuesByLabel = new LinkedHashMap<>();
        rows.forEach(row -> valuesByLabel.put(row.label(), safe(row.value())));

        return labels.stream()
                .map(label -> {
                    BigDecimal value = valuesByLabel.getOrDefault(label, BigDecimal.ZERO);
                    return new ChartPointDto(label, value, number(value, unit));
                })
                .toList();
    }

    private StatisticsFilter previousPeriodFilter(StatisticsFilter filter) {
        long days = ChronoUnit.DAYS.between(filter.startDate(), filter.endDate()) + 1;
        LocalDate previousEndDate = filter.startDate().minusDays(1);
        LocalDate previousStartDate = previousEndDate.minusDays(days - 1);

        return new StatisticsFilter(
                previousStartDate,
                previousEndDate,
                filter.departmentId(),
                filter.departmentIds(),
                filter.positionId(),
                filter.category(),
                filter.courseType()
        );
    }

    private String ratioHint(BigDecimal part, BigDecimal total) {
        BigDecimal safeTotal = safe(total);
        if (safeTotal.signum() == 0) {
            return "전체 임직원 대비 0%";
        }

        BigDecimal ratio = safe(part)
                .divide(safeTotal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return "전체 임직원 대비 " + StatisticsChartFactory.displayPercent(ratio);
    }

    private String previousPeriodHint(BigDecimal current, BigDecimal previous) {
        BigDecimal diff = safe(current).subtract(safe(previous));

        if (diff.signum() > 0) {
            return "지난 기간 대비 ▲ " + StatisticsChartFactory.displayPercent(diff);
        }

        if (diff.signum() < 0) {
            return "지난 기간 대비 ▼ " + StatisticsChartFactory.displayPercent(diff.abs());
        }

        return "지난 기간 대비 - 0%";
    }

    private String number(BigDecimal value, String unit) {
        return StatisticsChartFactory.display(value, unit);
    }

    private String percent(BigDecimal value) {
        return StatisticsChartFactory.displayPercent(value);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
