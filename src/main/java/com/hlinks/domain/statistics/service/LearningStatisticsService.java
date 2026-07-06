package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.KpiStatDto;
import com.hlinks.domain.statistics.dto.DepartmentGrowthQuery;
import com.hlinks.domain.statistics.dto.LearningKpiStats;
import com.hlinks.domain.statistics.dto.LearningStatisticsView;
import com.hlinks.domain.statistics.dto.StatisticsBlockDto;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsScope;
import com.hlinks.domain.statistics.dto.StatisticsSectionDto;
import com.hlinks.domain.statistics.mapper.LearningStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
                                StatisticsBlockDto.chart(4, StatisticsChartFactory.chart("learning-weekly-hours", "주간 학습량 추이", "학습 이력 직원 기준", "line", "h", "평균 학습 시간", learningStatisticsMapper.selectWeeklyLearningHours(filter)))
                        )),
                        new StatisticsSectionDto("참여 분석", List.of(
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-participation-funnel", "전직원 학습 참여 퍼널", "전체 임직원 대비 단계별 참여율", "funnel", "%", "참여율", learningStatisticsMapper.selectLearningParticipationFunnel(filter))),
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("learning-kcy-participation", "KCY 개발성향별 학습 참여율", "선택 기간 내 학습 참여 사용자 기준", "donut", "%", "참여율", learningStatisticsMapper.selectLearningParticipationRateByKcy(filter)))
                        )),
                        new StatisticsSectionDto("성장 분석", List.of(
                                StatisticsBlockDto.chart(1, StatisticsChartFactory.chart("learning-average-skill", "평균 역량", "Radar", "radar", "점", "평균 역량 점수", learningStatisticsMapper.selectAverageCompetencyScores(filter))),
                                StatisticsBlockDto.chart(3, StatisticsChartFactory.chart("learning-competency-growth-in-period", "기간 내 역량 성장률", "선택 기간 단위별 평균 역량 성장률", "line", "%", "성장률", learningStatisticsMapper.selectCompetencyGrowthInPeriod(DepartmentGrowthQuery.from(filter))))
                        ))
                )
        );
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
