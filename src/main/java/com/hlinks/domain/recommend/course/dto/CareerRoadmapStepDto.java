package com.hlinks.domain.recommend.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CareerRoadmapStepDto {

    private int stepOrder;
    private String title;
    private String description;
    private String targetSkillName;
    private String targetCoverageLevel;
    private boolean prerequisite;
    private boolean requiredPrerequisite;
    private List<RecommendedCourseDto> courses;
}
