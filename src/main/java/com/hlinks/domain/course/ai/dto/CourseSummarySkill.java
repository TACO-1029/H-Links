package com.hlinks.domain.course.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseSummarySkill {

    private String skillName;
    private String sourceSkillName;
    private String newSkillYn;
    private Integer weight;
}
