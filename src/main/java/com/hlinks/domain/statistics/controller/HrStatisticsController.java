package com.hlinks.domain.statistics.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.statistics.dto.ChartStatDto;
import com.hlinks.domain.statistics.dto.CourseStatisticsView;
import com.hlinks.domain.statistics.dto.LearningStatisticsView;
import com.hlinks.domain.statistics.dto.OrganizationStatisticsView;
import com.hlinks.domain.statistics.dto.StatisticsBlockDto;
import com.hlinks.domain.statistics.dto.StatisticsBlockKind;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsSectionDto;
import com.hlinks.domain.statistics.dto.StatisticsScope;
import com.hlinks.domain.statistics.service.CourseStatisticsService;
import com.hlinks.domain.statistics.service.LearningStatisticsService;
import com.hlinks.domain.statistics.service.OrganizationStatisticsService;
import com.hlinks.domain.statistics.service.StatisticsFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HrStatisticsController {

    private final LearningStatisticsService learningStatisticsService;
    private final CourseStatisticsService courseStatisticsService;
    private final OrganizationStatisticsService organizationStatisticsService;
    private final StatisticsFilterService statisticsFilterService;
    private final ObjectMapper objectMapper;

    @GetMapping("/hr/statistics/learning")
    public String learningStatistics(StatisticsFilter filter, Model model) {
        LearningStatisticsView view = learningStatisticsService.getLearningStatistics(filter, StatisticsScope.hrAdmin());

        model.addAttribute("activeMenu", "stats-learning");
        model.addAttribute("filter", filter);
        model.addAttribute("view", view);
        addFilterOptions(model);
        model.addAttribute("chartDataJson", toJson(extractCharts(view.sections())));
        return "hr/stats-learning";
    }

    @ResponseBody
    @GetMapping("/hr/statistics/learning/data")
    public LearningStatisticsView learningStatisticsData(StatisticsFilter filter) {
        return learningStatisticsService.getLearningStatistics(filter, StatisticsScope.hrAdmin());
    }

    @GetMapping("/hr/statistics/courses")
    public String courseStatistics(StatisticsFilter filter, Model model) {
        CourseStatisticsView view = courseStatisticsService.getCourseStatistics(filter, StatisticsScope.hrAdmin());

        model.addAttribute("activeMenu", "stats-course");
        model.addAttribute("filter", filter);
        model.addAttribute("view", view);
        addFilterOptions(model);
        model.addAttribute("chartDataJson", toJson(extractCharts(view.sections())));
        return "hr/stats-course";
    }

    @ResponseBody
    @GetMapping("/hr/statistics/courses/data")
    public CourseStatisticsView courseStatisticsData(StatisticsFilter filter) {
        return courseStatisticsService.getCourseStatistics(filter, StatisticsScope.hrAdmin());
    }

    @GetMapping("/hr/statistics/organizations")
    public String organizationStatistics(StatisticsFilter filter, Model model) {
        StatisticsFilter organizationFilter = defaultOrganizationFilter(filter);
        OrganizationStatisticsView view = organizationStatisticsService.getOrganizationStatistics(organizationFilter, StatisticsScope.hrAdmin());

        model.addAttribute("activeMenu", "stats-organization");
        model.addAttribute("filter", organizationFilter);
        model.addAttribute("view", view);
        addFilterOptions(model);
        model.addAttribute("departmentTree", statisticsFilterService.getDepartmentTreeOptions());
        model.addAttribute("chartDataJson", toJson(extractCharts(view.sections())));
        return "hr/stats-organization";
    }

    @ResponseBody
    @GetMapping("/hr/statistics/organizations/data")
    public OrganizationStatisticsView organizationStatisticsData(StatisticsFilter filter) {
        return organizationStatisticsService.getOrganizationStatistics(defaultOrganizationFilter(filter), StatisticsScope.hrAdmin());
    }

    private void addFilterOptions(Model model) {
        model.addAttribute("departments", statisticsFilterService.getDepartmentOptions());
        model.addAttribute("positions", statisticsFilterService.getPositionOptions());
    }

    private StatisticsFilter defaultOrganizationFilter(StatisticsFilter filter) {
        if (filter.hasDepartmentIds()) {
            return filter;
        }

        List<Long> defaultDepartmentIds = statisticsFilterService.getDefaultOrganizationDepartmentIds();
        if (defaultDepartmentIds.isEmpty()) {
            return filter.withDepartmentIds(List.of(-1L));
        }

        return filter.withDepartmentIds(defaultDepartmentIds);
    }

    private List<ChartStatDto> extractCharts(List<StatisticsSectionDto> sections) {
        return sections.stream()
                .flatMap(section -> section.blocks().stream())
                .filter(block -> block.kind() == StatisticsBlockKind.CHART)
                .map(StatisticsBlockDto::chart)
                .toList();
    }

    private String toJson(List<ChartStatDto> charts) {
        try {
            return objectMapper.writeValueAsString(charts);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
