package com.hlinks.domain.career.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CareerTargetSkillDto {
    private Long skillId;
    private String difficulty; // 하, 중, 상 or LOW, MEDIUM, HIGH
}
