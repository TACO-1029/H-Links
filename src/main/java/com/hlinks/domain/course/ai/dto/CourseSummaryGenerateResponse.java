package com.hlinks.domain.course.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CourseSummaryGenerateResponse {

    private String summaryText;
    private List<CourseSummarySkill> skills;
}
