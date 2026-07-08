package com.hlinks.domain.mypage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import com.hlinks.domain.mypage.ai.service.CompetencyAiSummaryService;
import com.hlinks.domain.mypage.dto.MyCompetencyEvaluationDto;
import com.hlinks.domain.mypage.mapper.MyCompetencyEvaluationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyCompetencyEvaluationService {

    private static final double RADAR_CENTER = 150.0;
    private static final double RADAR_RADIUS = 94.0;
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final MyCompetencyEvaluationMapper myCompetencyEvaluationMapper;
    private final CompetencyAiSummaryService competencyAiSummaryService;
    private final ObjectMapper objectMapper;

    @Cacheable(cacheNames = "competencyEvaluation", key = "#userId")
    public MyCompetencyEvaluationDto getEvaluation(Long userId) {
        List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores =
                enrichScores(myCompetencyEvaluationMapper.findCompetencyScores(userId));
        List<MyCompetencyEvaluationDto.ScoreHistoryDto> recentHistories =
                enrichHistories(myCompetencyEvaluationMapper.findRecentHistories(userId));
        List<MyCompetencyEvaluationDto.GrowthFactorDto> growthFactors =
                enrichGrowthFactors(myCompetencyEvaluationMapper.findGrowthFactors(userId));
        List<MyCompetencyEvaluationDto.ActionPlanDto> actionPlans =
                enrichActionPlans(myCompetencyEvaluationMapper.findRecommendedCoursesForWeakCompetencies(userId, 3), scores);

        MyCompetencyEvaluationDto.AiSummaryDto aiSummary = competencyAiSummaryService.summarize(
                scores,
                recentHistories,
                growthFactors,
                actionPlans
        );

        return MyCompetencyEvaluationDto.builder()
                .totalScore(calculateTotalScore(scores))
                .lastCalculatedAt(resolveLastCalculatedAt(userId))
                .radarPoints(toRadarPoints(scores, true))
                .organizationRadarPoints(toRadarPoints(scores, false))
                .radarChartDataJson(toRadarChartDataJson(scores))
                .scores(scores)
                .recentHistories(recentHistories)
                .growthFactors(growthFactors)
                .actionPlans(actionPlans)
                .aiSummary(aiSummary)
                .build();
    }

    private List<MyCompetencyEvaluationDto.CompetencyScoreDto> enrichScores(
            List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores
    ) {
        if (scores == null) {
            return List.of();
        }

        return scores.stream()
                .peek(score -> {
                    score.setLevelLabel(toLevelLabel(score.getUserScore()));
                    if (score.getTargetScore() <= 0) {
                        score.setTargetScore(100);
                    }
                })
                .toList();
    }

    private List<MyCompetencyEvaluationDto.ScoreHistoryDto> enrichHistories(
            List<MyCompetencyEvaluationDto.ScoreHistoryDto> recentHistories
    ) {
        if (recentHistories == null) {
            return List.of();
        }

        return recentHistories.stream()
                .peek(history -> history.setCalcTypeLabel(toCalcTypeLabel(history.getCalcType())))
                .toList();
    }

    private List<MyCompetencyEvaluationDto.GrowthFactorDto> enrichGrowthFactors(
            List<MyCompetencyEvaluationDto.GrowthFactorDto> growthFactors
    ) {
        if (growthFactors == null) {
            return List.of();
        }

        return growthFactors.stream()
                .peek(factor -> factor.setCalcTypeLabel(toCalcTypeLabel(factor.getCalcType())))
                .toList();
    }

    private List<MyCompetencyEvaluationDto.ActionPlanDto> enrichActionPlans(
            List<MyCompetencyEvaluationDto.ActionPlanDto> actionPlans,
            List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores
    ) {
        List<MyCompetencyEvaluationDto.ActionPlanDto> plans = new ArrayList<>();
        if (actionPlans != null) {
            plans.addAll(actionPlans);
        }

        if (plans.isEmpty()) {
            scores.stream()
                    .min(Comparator.comparingDouble(MyCompetencyEvaluationDto.CompetencyScoreDto::getUserScore))
                    .ifPresent(score -> plans.add(MyCompetencyEvaluationDto.ActionPlanDto.builder()
                            .type("ACTION")
                            .primarySkillName(score.getCompetencyName())
                            .courseTitle("맞춤 학습 진단으로 다음 강의 찾기")
                            .reason(score.getCompetencyName() + "을 기준으로 레벨테스트 또는 추천 강의를 확인해보세요.")
                            .recommendationScore(0)
                            .build()));

            plans.add(MyCompetencyEvaluationDto.ActionPlanDto.builder()
                    .type("ACTION")
                    .primarySkillName("학습 습관")
                    .courseTitle("7일 연속 학습 목표 설정")
                    .reason("짧은 학습을 꾸준히 이어가면 자기개발역량 성장에 도움이 됩니다.")
                    .recommendationScore(0)
                    .build());
        }

        return plans.stream()
                .limit(3)
                .toList();
    }

    private int calculateTotalScore(List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores) {
        if (scores.isEmpty()) {
            return 70;
        }

        double average = scores.stream()
                .mapToDouble(MyCompetencyEvaluationDto.CompetencyScoreDto::getUserScore)
                .average()
                .orElse(70);

        return BigDecimal.valueOf(average)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String resolveLastCalculatedAt(Long userId) {
        String lastCalculatedAt = myCompetencyEvaluationMapper.findLastCalculatedAt(userId);
        if (StringUtils.hasText(lastCalculatedAt)) {
            return lastCalculatedAt;
        }
        return LocalDate.now().format(DISPLAY_DATE_FORMATTER);
    }

    private String toRadarPoints(List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores, boolean userScore) {
        if (scores.isEmpty()) {
            return "";
        }

        StringBuilder points = new StringBuilder();
        int count = scores.size();
        for (int i = 0; i < count; i++) {
            MyCompetencyEvaluationDto.CompetencyScoreDto score = scores.get(i);
            double value = userScore ? score.getUserScore() : score.getOrganizationAverageScore();
            double ratio = Math.max(0, Math.min(100, value)) / 100.0;
            double angle = Math.toRadians(-90 + ((360.0 / count) * i));
            double x = RADAR_CENTER + Math.cos(angle) * RADAR_RADIUS * ratio;
            double y = RADAR_CENTER + Math.sin(angle) * RADAR_RADIUS * ratio;
            if (points.length() > 0) {
                points.append(' ');
            }
            points.append(roundCoordinate(x)).append(',').append(roundCoordinate(y));
        }
        return points.toString();
    }

    private String toRadarChartDataJson(List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores) {
        Map<String, Object> chartData = Map.of(
                "labels", scores.stream()
                        .map(MyCompetencyEvaluationDto.CompetencyScoreDto::getCompetencyName)
                        .toList(),
                "userScores", scores.stream()
                        .map(MyCompetencyEvaluationDto.CompetencyScoreDto::getUserScore)
                        .toList(),
                "organizationScores", scores.stream()
                        .map(MyCompetencyEvaluationDto.CompetencyScoreDto::getOrganizationAverageScore)
                        .toList()
        );

        try {
            return objectMapper.writeValueAsString(chartData);
        } catch (JsonProcessingException e) {
            return "{\"labels\":[],\"userScores\":[],\"organizationScores\":[]}";
        }
    }

    private String roundCoordinate(double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .toPlainString();
    }

    private String toLevelLabel(double score) {
        if (score >= 90) {
            return "탁월";
        }
        if (score >= 80) {
            return "우수";
        }
        if (score >= 70) {
            return "보통";
        }
        return "보완";
    }

    private String toCalcTypeLabel(String calcType) {
        if (!StringUtils.hasText(calcType)) {
            return "역량 점수 반영";
        }

        for (CompetencyCalcType type : CompetencyCalcType.values()) {
            if (type.getCode().equals(calcType)) {
                return type.getLabel();
            }
        }
        return calcType;
    }
}
