package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseSkillAggregationRow {

    private Long chapterId;
    private Long skillId;
    private Integer weight;
    private Integer durationSeconds;
}
