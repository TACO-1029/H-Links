package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.ChartPointDto;
import com.hlinks.domain.statistics.dto.ChartSeriesDto;
import com.hlinks.domain.statistics.dto.ChartStatDto;
import com.hlinks.domain.statistics.dto.CoursePeriodQuery;
import com.hlinks.domain.statistics.dto.CourseStatisticsView;
import com.hlinks.domain.statistics.dto.KpiStatDto;
import com.hlinks.domain.statistics.dto.PopularCourseRow;
import com.hlinks.domain.statistics.dto.RankStatDto;
import com.hlinks.domain.statistics.dto.SkillPopularityChangeQuery;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import com.hlinks.domain.statistics.dto.StatisticsRankRow;
import com.hlinks.domain.statistics.dto.StatisticsBlockDto;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsScope;
import com.hlinks.domain.statistics.dto.StatisticsSectionDto;
import com.hlinks.domain.statistics.mapper.CourseStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseStatisticsService {

    private final CourseStatisticsMapper courseStatisticsMapper;

    public CourseStatisticsView getCourseStatistics(StatisticsFilter filter, StatisticsScope scope) {
        StatisticsFilter previousFilter = previousPeriodFilter(filter);
        BigDecimal operatingCourses = courseStatisticsMapper.countOperatingCourses(filter);
        BigDecimal courseApplications = courseStatisticsMapper.countCourseApplications(filter);
        BigDecimal previousCourseApplications = courseStatisticsMapper.countCourseApplications(previousFilter);
        BigDecimal completionRate = courseStatisticsMapper.selectCourseCompletionRate(filter);
        BigDecimal previousCompletionRate = courseStatisticsMapper.selectCourseCompletionRate(previousFilter);
        BigDecimal averageProgressRate = courseStatisticsMapper.selectAverageProgressRate(filter);
        BigDecimal previousAverageProgressRate = courseStatisticsMapper.selectAverageProgressRate(previousFilter);

        return new CourseStatisticsView(
                "강의 통계",
                "강의별 신청, 수강, 수료 흐름과 콘텐츠 성과를 확인합니다.",
                List.of(
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-journal-bookmark-fill", "현재 운영 중인 강의 수", number(operatingCourses, "개"), "현재 OPEN 상태 강의 기준", "green")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-people-fill", "총 수강 신청 건수", number(courseApplications, "건"), countComparisonHint(courseApplications, previousCourseApplications, "건"), "blue")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-mortarboard-fill", "전체 수료율 (평균)", percent(completionRate), percentComparisonHint(completionRate, previousCompletionRate), "purple")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-play-fill", "전체 평균 진도율", percent(averageProgressRate), percentComparisonHint(averageProgressRate, previousAverageProgressRate), "orange"))
                ),
                List.of(
                        new StatisticsSectionDto("강의 운영 및 성과", List.of(
                                StatisticsBlockDto.chart(1, StatisticsChartFactory.chart("course-type-status", "강의 유형별 현황", "현재 OPEN 강의 기준", "donut", "개", "개설 강의 수", courseStatisticsMapper.selectOperatingCoursesByType(filter))),
                                StatisticsBlockDto.chart(1, StatisticsChartFactory.chart("course-type-completion", "강의 유형별 수료율", "온라인/오프라인", "bar", "%", "수료율", courseStatisticsMapper.selectCourseTypeCompletionRate(filter))),
                                StatisticsBlockDto.chart(2, applicationCompletionConversionTrend(courseStatisticsMapper.selectApplicationCompletionConversionTrend(CoursePeriodQuery.from(filter))))
                        )),
                        new StatisticsSectionDto("강의 인기도 및 수요", List.of(
                                StatisticsBlockDto.rank(3, "인기 강의 TOP 5", "수강 신청 기준", popularCourseRanks(courseStatisticsMapper.selectPopularCourses(filter))),
                                StatisticsBlockDto.rank(1, "미수료/이탈 강의 TOP 5", "미수료/이탈 건수 기준", incompleteCourseRanks(courseStatisticsMapper.selectIncompleteCourses(filter))),
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("course-popular-skills", "인기 스킬 TOP 5", "수강 신청 수 기준", "bar", "명", "수강 신청 수", courseStatisticsMapper.selectPopularSkills(filter))),
                                StatisticsBlockDto.chart(2, risingSkills(courseStatisticsMapper.selectRisingSkills(SkillPopularityChangeQuery.from(filter, previousFilter))))
                        ))
                )
        );
    }

    private ChartStatDto applicationCompletionConversionTrend(List<StatisticsPointRow> rows) {
        List<ChartPointDto> points = rows.stream()
                .map(row -> new ChartPointDto(
                        row.label(),
                        safe(row.value()),
                        percent(row.value())
                ))
                .toList();

        return new ChartStatDto(
                "course-application-completion-conversion",
                "강의 신청 대비 수료 전환율 추이",
                "선택 기간 내 수료 건수 / 신청 건수",
                "line",
                "%",
                List.of(new ChartSeriesDto("전환율", points))
        );
    }

    private ChartStatDto risingSkills(List<StatisticsPointRow> rows) {
        List<ChartPointDto> points = rows.stream()
                .map(row -> new ChartPointDto(
                        row.label(),
                        safe(row.value()),
                        signedNumber(row.value(), "명")
                ))
                .toList();

        return new ChartStatDto(
                "course-rising-skills",
                "급상승 스킬 TOP 5",
                "이전 동일 기간 대비 신청 증가 수",
                "bar",
                "명",
                List.of(new ChartSeriesDto("증가 수", points))
        );
    }

    private List<RankStatDto> popularCourseRanks(List<PopularCourseRow> rows) {
        return rows.stream()
                .map(row -> new RankStatDto(
                        row.rank(),
                        null,
                        null,
                        row.courseTitle(),
                        number(row.applicationCount(), "명"),
                        row.categoryName(),
                        row.courseTypeName(),
                        number(row.applicationCount(), "명"),
                        percent(row.completionRate()),
                        percent(row.averageProgressRate()),
                        percent(row.quizCorrectRate())
                ))
                .toList();
    }

    private List<RankStatDto> incompleteCourseRanks(List<StatisticsRankRow> rows) {
        return rows.stream()
                .map(row -> new RankStatDto(
                        row.rank(),
                        row.badgeText(),
                        row.badgeTone(),
                        row.label(),
                        number(row.value(), "명")
                ))
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

    private String countComparisonHint(BigDecimal current, BigDecimal previous, String unit) {
        BigDecimal diff = safe(current).subtract(safe(previous));
        return "지난 기간 대비 " + signedNumber(diff, unit) + " (" + signedPercentChange(current, previous) + ")";
    }

    private String percentComparisonHint(BigDecimal current, BigDecimal previous) {
        BigDecimal diff = safe(current).subtract(safe(previous));
        return "지난 기간 대비 " + signedNumber(diff, "%p") + " (" + signedPercentChange(current, previous) + ")";
    }

    private String signedNumber(BigDecimal value, String unit) {
        String sign = value.signum() > 0 ? "+" : "";
        return sign + number(value, unit);
    }

    private String signedPercentChange(BigDecimal current, BigDecimal previous) {
        BigDecimal safePrevious = safe(previous);
        if (safePrevious.signum() == 0) {
            return safe(current).signum() == 0 ? "0%" : "+100%";
        }

        BigDecimal changeRate = safe(current)
                .subtract(safePrevious)
                .divide(safePrevious.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        String sign = changeRate.signum() > 0 ? "+" : "";
        return sign + StatisticsChartFactory.displayPercent(changeRate);
    }

    private String signedPercent(BigDecimal value) {
        BigDecimal safeValue = safe(value);
        String sign = safeValue.signum() > 0 ? "+" : "";
        return sign + StatisticsChartFactory.displayPercent(safeValue);
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
