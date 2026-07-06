package com.hlinks.domain.statistics.dto;

public record DepartmentFilterNodeDto(
        Long id,
        Long parentId,
        String name,
        int depth
) {
}
