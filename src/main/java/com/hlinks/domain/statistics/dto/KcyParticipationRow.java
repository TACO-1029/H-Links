package com.hlinks.domain.statistics.dto;

import java.math.BigDecimal;

public record KcyParticipationRow(
        String label,
        Long participantCount,
        BigDecimal participationRate
) {
}
