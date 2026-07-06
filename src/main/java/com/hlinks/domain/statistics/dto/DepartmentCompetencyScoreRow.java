package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record DepartmentCompetencyScoreRow(
        String departmentName,
        String competencyName,
        BigDecimal score
) {
}
