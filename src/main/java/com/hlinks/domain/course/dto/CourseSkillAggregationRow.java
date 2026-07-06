package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseSkillAggregationRow {

    private Long chapterId;
    private Long skillId;
    private BigDecimal weight;
    private String coverageLevel;
    private String coverageReason;
    private Integer durationSeconds;
}
