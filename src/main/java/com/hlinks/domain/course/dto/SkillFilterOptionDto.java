package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillFilterOptionDto {

    private Long parentSkillId;
    private String parentSkillName;
    private Long skillId;
    private String skillName;
}
