package com.hlinks.domain.statistics.dto;

import java.util.List;

public record RankTableRowDto(
        String rankLabel,
        List<String> values
) {
}
