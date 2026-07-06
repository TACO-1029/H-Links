package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record DepartmentGrowthPointRow(
        String departmentName,
        String periodLabel,
        BigDecimal growthRate
) {
}
