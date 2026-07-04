package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SkillFilterGroupDto {

    private Long skillId;
    private String skillName;
    private List<SkillFilterOptionDto> skills;
}
