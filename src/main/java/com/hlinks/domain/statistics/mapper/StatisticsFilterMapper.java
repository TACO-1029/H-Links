package com.hlinks.domain.statistics.mapper;

import com.hlinks.domain.statistics.dto.DepartmentFilterNodeDto;
import com.hlinks.domain.statistics.dto.FilterOptionDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatisticsFilterMapper {

    List<FilterOptionDto> selectDepartmentOptions();

    List<DepartmentFilterNodeDto> selectDepartmentTreeOptions();

    List<Long> selectDefaultOrganizationDepartmentIds();

    List<FilterOptionDto> selectPositionOptions();
}
