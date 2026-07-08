package com.hlinks.domain.hr.mapper;

import com.hlinks.domain.hr.dto.AdminCourseCompletionStatusRow;
import com.hlinks.domain.hr.dto.AdminDashboardFilter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminDashboardMapper {

    int countCourseCompletions(@Param("filter") AdminDashboardFilter filter);

    List<AdminCourseCompletionStatusRow> selectCourseCompletions(
            @Param("filter") AdminDashboardFilter filter,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );
}
