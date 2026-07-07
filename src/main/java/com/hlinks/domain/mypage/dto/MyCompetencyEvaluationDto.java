package com.hlinks.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCompetencyEvaluationDto {

    private int totalScore;
    private String lastCalculatedAt;
    private String radarPoints;
    private String organizationRadarPoints;
    private String radarChartDataJson;
    private List<CompetencyScoreDto> scores;
    private List<ScoreHistoryDto> recentHistories;
    private List<GrowthFactorDto> growthFactors;
    private List<ActionPlanDto> actionPlans;
    private AiSummaryDto aiSummary;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompetencyScoreDto {
        private Long competencyId;
        private String competencyName;
        private String description;
        private double userScore;
        private double organizationAverageScore;
        private double targetScore;
        private double gapToOrganization;
        private double gapToTarget;
        private int displayOrder;
        private String levelLabel;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreHistoryDto {
        private Long historyId;
        private Long competencyId;
        private String competencyName;
        private double score;
        private double scoreDelta;
        private String calcType;
        private String calcTypeLabel;
        private String referenceType;
        private Long referenceId;
        private String calculatedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthFactorDto {
        private String calcType;
        private String calcTypeLabel;
        private double scoreDelta;
        private int eventCount;
        private String latestCalculatedAt;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionPlanDto {
        private String type;
        private Long courseId;
        private String courseTitle;
        private String courseType;
        private String primarySkillName;
        private String reason;
        private double recommendationScore;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiSummaryDto {
        private String headline;
        private String strength;
        private String improvement;
        private String nextAction;
        private boolean fallback;
    }
}
