package com.hlinks.domain.statistics.dto;

public record KpiStatDto(
        String icon,
        String label,
        String value,
        String hint,
        String tone
) {
}
