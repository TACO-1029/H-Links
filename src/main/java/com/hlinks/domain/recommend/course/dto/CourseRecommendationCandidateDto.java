package com.hlinks.domain.recommend.course.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseRecommendationCandidateDto {

    private Long courseId;
    private String categoryType;
    private String courseType;
    private String courseTitle;
    private String description;
    private String instructorName;
    private String thumbnailUrl;
    private Long skillId;
    private String skillName;
    private BigDecimal weight;
    private String coverageLevel;
    private String coverageReason;
}
