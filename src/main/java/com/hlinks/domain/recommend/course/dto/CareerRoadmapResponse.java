package com.hlinks.domain.recommend.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CareerRoadmapResponse {

    private String category;
    private String summary;
    private int requestedSkillCount;
    private int stepCount;
    private List<CareerRoadmapStepDto> steps;
}
