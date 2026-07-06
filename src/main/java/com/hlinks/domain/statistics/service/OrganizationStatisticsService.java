package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.KpiStatDto;
import com.hlinks.domain.statistics.dto.ChartPointDto;
import com.hlinks.domain.statistics.dto.ChartSeriesDto;
import com.hlinks.domain.statistics.dto.ChartStatDto;
import com.hlinks.domain.statistics.dto.DepartmentCompetencyScoreRow;
import com.hlinks.domain.statistics.dto.DepartmentCourseRankRow;
import com.hlinks.domain.statistics.dto.DepartmentGrowthPointRow;
import com.hlinks.domain.statistics.dto.DepartmentGrowthQuery;
import com.hlinks.domain.statistics.dto.OrganizationKpiStats;
import com.hlinks.domain.statistics.dto.OrganizationStatisticsView;
import com.hlinks.domain.statistics.dto.RankStatDto;
import com.hlinks.domain.statistics.dto.RankTableDto;
import com.hlinks.domain.statistics.dto.RankTableRowDto;
import com.hlinks.domain.statistics.dto.StatisticsBlockDto;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsScope;
import com.hlinks.domain.statistics.dto.StatisticsSectionDto;
import com.hlinks.domain.statistics.mapper.OrganizationStatisticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationStatisticsService {

    private final OrganizationStatisticsMapper organizationStatisticsMapper;

    public OrganizationStatisticsView getOrganizationStatistics(StatisticsFilter filter, StatisticsScope scope) {
        OrganizationKpiStats kpis = organizationStatisticsMapper.selectOrganizationKpis(filter);

        return new OrganizationStatisticsView(
                "부서 비교 통계",
                "선택한 부서 단위로 학습 참여도와 역량 성장 흐름을 비교합니다.",
                List.of(
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-trophy-fill", "참여율 1위 부서", fallback(kpis.topParticipationDepartmentName(), "참여 데이터 없음"), percentWithAverageHint(kpis.topParticipationRate(), kpis.averageParticipationRate()), "")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-mortarboard-fill", "수료율 1위 부서", fallback(kpis.topCompletionDepartmentName(), "수료 데이터 없음"), percentWithAverageHint(kpis.topCompletionRate(), kpis.averageCompletionRate()), "blue")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-star-fill", "평균 역량 점수 1위 부서", fallback(kpis.topCompetencyDepartmentName(), "역량 데이터 없음"), scoreWithAverageHint(kpis.topCompetencyScore(), kpis.averageCompetencyScore()), "purple")),
                        StatisticsBlockDto.kpi(1, new KpiStatDto("bi bi-graph-up-arrow", "성장률 1위 부서", fallback(kpis.topGrowthDepartmentName(), "성장 데이터 없음"), growthWithAverageHint(kpis.topGrowthRate(), kpis.averageGrowthRate()), "orange"))
                ),
                List.of(
                        new StatisticsSectionDto("부서별 비교", List.of(
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("organization-department-participation", "부서별 학습 참여율", "참여 인원 / 대상 인원", "bar", "%", "참여율", organizationStatisticsMapper.selectDepartmentParticipationRate(filter))),
                                StatisticsBlockDto.chart(2, StatisticsChartFactory.chart("organization-department-completion", "부서별 수료율", "수료자 비율", "bar", "%", "수료율", organizationStatisticsMapper.selectDepartmentCompletionRate(filter)))
                        )),
                        new StatisticsSectionDto("역량 및 성장", List.of(
                                StatisticsBlockDto.chart(2, departmentCompetencyRadar(organizationStatisticsMapper.selectDepartmentAverageCompetencyScores(filter))),
                                StatisticsBlockDto.chart(2, departmentGrowthLine(organizationStatisticsMapper.selectDepartmentGrowthRates(DepartmentGrowthQuery.from(filter))))
                        )),
                        new StatisticsSectionDto("교육 성과", List.of(
                                StatisticsBlockDto.rank(1, "참여 우수 부서 랭킹", "참여율 기준", StatisticsChartFactory.ranks(organizationStatisticsMapper.selectTopParticipationDepartments(filter), "%")),
                                StatisticsBlockDto.rankTable(3, "부서별 인기 강의 랭킹", "강의 신청 수 기준", departmentCourseRankTable(organizationStatisticsMapper.selectPopularCoursesByDepartment(filter), filter))
                        ))
                )
        );
    }

    private ChartStatDto departmentCompetencyRadar(List<DepartmentCompetencyScoreRow> rows) {
        Map<String, List<ChartPointDto>> pointsByDepartment = new LinkedHashMap<>();

        for (DepartmentCompetencyScoreRow row : rows) {
            pointsByDepartment
                    .computeIfAbsent(row.departmentName(), ignored -> new ArrayList<>())
                    .add(new ChartPointDto(
                            row.competencyName(),
                            safe(row.score()),
                            number(row.score(), "점")
                    ));
        }

        List<ChartSeriesDto> series = pointsByDepartment.entrySet().stream()
                .map(entry -> new ChartSeriesDto(entry.getKey(), entry.getValue()))
                .toList();

        return new ChartStatDto(
                "organization-department-average-competency",
                "부서별 평균 역량",
                "5개 핵심 역량 평균 점수",
                "heatmap",
                "점",
                series
        );
    }

    private ChartStatDto departmentGrowthLine(List<DepartmentGrowthPointRow> rows) {
        Map<String, List<ChartPointDto>> pointsByDepartment = new LinkedHashMap<>();

        for (DepartmentGrowthPointRow row : rows) {
            pointsByDepartment
                    .computeIfAbsent(row.departmentName(), ignored -> new ArrayList<>())
                    .add(new ChartPointDto(
                            row.periodLabel(),
                            safe(row.growthRate()),
                            signedPercent(row.growthRate())
                    ));
        }

        List<ChartSeriesDto> series = pointsByDepartment.entrySet().stream()
                .map(entry -> new ChartSeriesDto(entry.getKey(), entry.getValue()))
                .toList();

        return new ChartStatDto(
                "organization-department-growth-rate",
                "부서별 성장률 순위 변화",
                "월별 성장률 순위와 전사 평균 대비 여부",
                "line",
                "%",
                series
        );
    }

    private List<RankStatDto> departmentCourseRanks(List<DepartmentCourseRankRow> rows) {
        return rows.stream()
                .map(row -> new RankStatDto(
                        row.rank(),
                        row.departmentName(),
                        badgeTone(row.rank()),
                        row.courseTitle(),
                        number(row.value(), "명")
                ))
                .toList();
    }

    private RankTableDto departmentCourseRankTable(List<DepartmentCourseRankRow> rows, StatisticsFilter filter) {
        Set<String> departmentNames = new LinkedHashSet<>();
        Map<Integer, Map<String, String>> courseTitlesByRankAndDepartment = new LinkedHashMap<>();

        for (DepartmentCourseRankRow row : rows) {
            departmentNames.add(row.departmentName());
            courseTitlesByRankAndDepartment
                    .computeIfAbsent(row.rank(), ignored -> new LinkedHashMap<>())
                    .put(row.departmentName(), row.courseTitle());
        }

        List<String> columns = List.copyOf(departmentNames);
        int titleMaxLength = courseTitleMaxLength(filter, columns.size());
        List<RankTableRowDto> tableRows = java.util.stream.IntStream.rangeClosed(1, 5)
                .mapToObj(rank -> {
                    Map<String, String> courseTitlesByDepartment = courseTitlesByRankAndDepartment.getOrDefault(rank, Map.of());
                    List<String> values = columns.stream()
                            .map(departmentName -> shortenCourseTitle(courseTitlesByDepartment.get(departmentName), titleMaxLength))
                            .toList();
                    return new RankTableRowDto(rank + "위", values);
                })
                .toList();

        return new RankTableDto(columns, tableRows);
    }

    private int courseTitleMaxLength(StatisticsFilter filter, int displayedDepartmentCount) {
        int selectedDepartmentCount = filter != null && filter.hasDepartmentIds()
                ? filter.departmentIds().size()
                : displayedDepartmentCount;
        int departmentCount = Math.max(1, Math.min(selectedDepartmentCount, 5));

        return switch (departmentCount) {
            case 1 -> 34;
            case 2 -> 26;
            case 3 -> 20;
            case 4 -> 16;
            default -> 13;
        };
    }

    private String shortenCourseTitle(String title, int maxLength) {
        if (title == null || title.isBlank()) {
            return "-";
        }

        String trimmed = title.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) + "..." : trimmed;
    }

    private String badgeTone(int rank) {
        return switch (rank % 4) {
            case 1 -> "green";
            case 2 -> "blue";
            case 3 -> "orange";
            default -> "purple";
        };
    }

    private String number(BigDecimal value, String unit) {
        return StatisticsChartFactory.display(value, unit);
    }

    private String percent(BigDecimal value) {
        return StatisticsChartFactory.displayPercent(value);
    }

    private String percentWithAverageHint(BigDecimal value, BigDecimal average) {
        return percent(value) + " · 전체 평균 " + percent(average) + " · " + delta(value, average, "%p");
    }

    private String scoreWithAverageHint(BigDecimal value, BigDecimal average) {
        return number(value, "점") + " · 전체 평균 " + number(average, "점") + " · " + delta(value, average, "점");
    }

    private String growthWithAverageHint(BigDecimal value, BigDecimal average) {
        return signedPercent(value) + " · 전체 평균 " + signedPercent(average) + " · " + delta(value, average, "%p");
    }

    private String signedPercent(BigDecimal value) {
        BigDecimal safeValue = safe(value);
        String sign = safeValue.signum() > 0 ? "+" : "";
        return sign + StatisticsChartFactory.displayPercent(safeValue);
    }

    private String delta(BigDecimal value, BigDecimal average, String unit) {
        BigDecimal diff = safe(value).subtract(safe(average));
        if (diff.signum() > 0) {
            return "▲ " + number(diff, unit);
        }

        if (diff.signum() < 0) {
            return "▼ " + number(diff.abs(), unit);
        }

        return "- 0" + unit;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
