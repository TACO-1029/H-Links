package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record LearningKpiStats(
        BigDecimal activeLearners,
        BigDecimal totalLearners,
        BigDecimal completionRate,
        BigDecimal averageProgressRate,
        BigDecimal quizCorrectRate
) {
}
