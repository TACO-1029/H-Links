package com.hlinks.domain.statistics.dto;

import java.util.List;

public record RankTableDto(
        List<String> columns,
        List<RankTableRowDto> rows
) {
}
