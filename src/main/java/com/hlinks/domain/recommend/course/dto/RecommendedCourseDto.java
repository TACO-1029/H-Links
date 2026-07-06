package com.hlinks.domain.recommend.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendedCourseDto {

    private Long courseId;
    private String categoryType;
    private String courseType;
    private String courseTitle;
    private String instructorName;
    private String thumbnailUrl;
    private double recommendationScore;
    private String reason;
    private List<RecommendedCourseSkillDto> matchedSkills;
}
