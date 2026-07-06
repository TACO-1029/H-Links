package com.hlinks.domain.statistics.service;

import com.hlinks.domain.statistics.dto.DepartmentFilterNodeDto;
import com.hlinks.domain.statistics.dto.FilterOptionDto;
import com.hlinks.domain.statistics.mapper.StatisticsFilterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsFilterService {

    private final StatisticsFilterMapper statisticsFilterMapper;

    public List<FilterOptionDto> getDepartmentOptions() {
        return statisticsFilterMapper.selectDepartmentOptions();
    }

    public List<DepartmentFilterNodeDto> getDepartmentTreeOptions() {
        return statisticsFilterMapper.selectDepartmentTreeOptions();
    }

    public List<Long> getDefaultOrganizationDepartmentIds() {
        return statisticsFilterMapper.selectDefaultOrganizationDepartmentIds();
    }

    public List<FilterOptionDto> getPositionOptions() {
        return statisticsFilterMapper.selectPositionOptions();
    }
}
