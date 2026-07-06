package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record OrganizationKpiStats(
        BigDecimal departmentCount,
        String topParticipationDepartmentName,
        BigDecimal topParticipationRate,
        BigDecimal averageParticipationRate,
        String topCompletionDepartmentName,
        BigDecimal topCompletionRate,
        BigDecimal averageCompletionRate,
        String topCompetencyDepartmentName,
        BigDecimal topCompetencyScore,
        BigDecimal averageCompetencyScore,
        String topGrowthDepartmentName,
        BigDecimal topGrowthRate,
        BigDecimal averageGrowthRate
) {
}
