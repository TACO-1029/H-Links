package com.hlinks.domain.course.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseSummarySkill {

    private String skillName;
    private String sourceSkillName;
    private String newSkillYn;
    private BigDecimal weight;
    private String coverageLevel;
    private String coverageReason;
}
