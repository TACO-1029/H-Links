package com.hlinks.domain.recommend.kcy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KcyPartnerRecommendationDto {

    private Long userId;
    private String name;
    private String displayName;
    private String departmentName;
    private String jobName;
    private String positionName;
    private String kcyCode;
    private String kcyTitle;
    private String grade;
    private String gradeLabel;
    private int score;
    private String reason;
}
