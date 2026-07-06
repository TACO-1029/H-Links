package com.hlinks.domain.recommend.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class RecommendedCourseSkillDto {

    private Long skillId;
    private String skillName;
    private int userScore;
    private String selectedDifficulty;
    private String recommendedCoverageLevel;
    private String courseCoverageLevel;
    private BigDecimal courseSkillWeight;
    private double deficiencyScore;
    private double coverageMatchScore;
    private double contributionScore;
}
