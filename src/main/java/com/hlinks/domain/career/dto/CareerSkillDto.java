package com.hlinks.domain.career.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerSkillDto {
    private Long skillId;
    private String skillName;
    private Long parentSkillId;
    private String skillType;
}
