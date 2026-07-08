package com.hlinks.domain.hr.service;

import com.hlinks.domain.hr.dto.AdminDashboardFilter;
import com.hlinks.domain.hr.dto.AdminCourseCompletionStatusRow;
import com.hlinks.domain.hr.mapper.AdminDashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public int countCourseCompletions(AdminDashboardFilter filter) {
        return adminDashboardMapper.countCourseCompletions(filter);
    }

    public List<AdminCourseCompletionStatusRow> getCourseCompletions(AdminDashboardFilter filter, int offset, int pageSize) {
        return adminDashboardMapper.selectCourseCompletions(filter, offset, pageSize);
    }
}
