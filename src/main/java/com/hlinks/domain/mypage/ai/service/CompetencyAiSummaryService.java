package com.hlinks.domain.mypage.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.mypage.ai.CompetencyAiSummaryProperties;
import com.hlinks.domain.mypage.ai.dto.CompetencyAiSummaryResponse;
import com.hlinks.domain.mypage.dto.MyCompetencyEvaluationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetencyAiSummaryService {

    private final CompetencyAiSummaryProperties properties;
    private final ObjectMapper objectMapper;

    public MyCompetencyEvaluationDto.AiSummaryDto summarize(
            List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores,
            List<MyCompetencyEvaluationDto.ScoreHistoryDto> recentHistories,
            List<MyCompetencyEvaluationDto.GrowthFactorDto> growthFactors,
            List<MyCompetencyEvaluationDto.ActionPlanDto> actionPlans
    ) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            log.info("Competency AI summary API key is empty. Returning fallback summary.");
            return buildFallback(scores, growthFactors, actionPlans);
        }

        String rawResponse = null;
        try {
            rawResponse = createRestClient().post()
                    .uri(properties.getApiUrl())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createRequestBody(buildPrompt(scores, recentHistories, growthFactors, actionPlans)))
                    .retrieve()
                    .body(String.class);
            log.debug("Competency AI summary rawResponse={}", rawResponse);

            String content = extractMessageContent(rawResponse);
            log.debug("Competency AI summary messageContent={}", content);

            CompetencyAiSummaryResponse response = objectMapper.readValue(
                    content,
                    CompetencyAiSummaryResponse.class
            );

            if (!StringUtils.hasText(response.getHeadline())) {
                log.warn("Competency AI summary response has no headline. Returning fallback summary. content={}", content);
                return buildFallback(scores, growthFactors, actionPlans);
            }

            return MyCompetencyEvaluationDto.AiSummaryDto.builder()
                    .headline(response.getHeadline())
                    .strength(defaultText(response.getStrength(), "현재 점수 기준 강점 역량을 안정적으로 유지하고 있습니다."))
                    .improvement(defaultText(response.getImprovement(), "조직 평균보다 낮은 역량을 중심으로 학습 우선순위를 잡아보세요."))
                    .nextAction(defaultText(response.getNextAction(), "추천 강의와 최근 성장 요인을 참고해 다음 학습을 선택해보세요."))
                    .growthPotential(defaultText(response.getGrowthPotential(), "현재 점수와 최근 학습 이력을 바탕으로 약한 역량부터 보완하면 성장 가능성이 있습니다."))
                    .fallback(false)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to generate competency AI summary. provider={}, model={}, rawResponse={}",
                    properties.getProvider(),
                    properties.getModel(),
                    rawResponse,
                    e
            );
            return buildFallback(scores, growthFactors, actionPlans);
        }
    }

    private Map<String, Object> createRequestBody(String prompt) {
        return Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "당신은 기업 학습 플랫폼의 역량 진단 코치입니다. 반드시 유효한 JSON만 반환하세요."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );
    }

    private String buildPrompt(
            List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores,
            List<MyCompetencyEvaluationDto.ScoreHistoryDto> recentHistories,
            List<MyCompetencyEvaluationDto.GrowthFactorDto> growthFactors,
            List<MyCompetencyEvaluationDto.ActionPlanDto> actionPlans
    ) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            아래 사용자의 역량 점수와 최근 학습 반영 이력을 바탕으로 한국어 요약을 작성하세요.
    
            응답 JSON 형식:
            {
              "headline": "2~3줄 분량의 종합 요약",
              "strength": "강점 요약",
              "improvement": "보완 방향",
              "growthPotential": "성장 가능성 요약",
              "nextAction": "다음 행동 제안"
            }
    
            작성 규칙:
            - 반드시 위 JSON 키를 모두 포함하세요.
            - headline은 120~180자 정도로 2~3문장 작성하세요.
            - strength, improvement, growthPotential, nextAction은 각각 80자 이내로 작성하세요.
            - growthPotential은 현재 점수, 최근 성장 요인, 추천 액션을 바탕으로 앞으로 어떤 역량이 더 성장할 수 있는지 작성하세요.
            - 과장하지 말고, 제공된 데이터에 근거해서 작성하세요.
    
            [역량 점수]
            """);

        for (MyCompetencyEvaluationDto.CompetencyScoreDto score : scores) {
            prompt.append("- ")
                    .append(score.getCompetencyName())
                    .append(": 내 점수 ")
                    .append(score.getUserScore())
                    .append(", 조직 평균 ")
                    .append(score.getOrganizationAverageScore())
                    .append('\n');
        }

        prompt.append("\n[최근 성장 요인]\n");
        if (growthFactors.isEmpty()) {
            prompt.append("- 최근 30일 내 점수 상승 요인 없음\n");
        } else {
            for (MyCompetencyEvaluationDto.GrowthFactorDto factor : growthFactors) {
                prompt.append("- ")
                        .append(factor.getCalcTypeLabel())
                        .append(": +")
                        .append(factor.getScoreDelta())
                        .append(", ")
                        .append(factor.getEventCount())
                        .append("회\n");
            }
        }

        prompt.append("\n[최근 반영 이력]\n");
        recentHistories.stream().limit(4).forEach(history -> prompt.append("- ")
                .append(history.getCompetencyName())
                .append(" / ")
                .append(history.getCalcTypeLabel())
                .append(" / +")
                .append(history.getScoreDelta())
                .append('\n'));

        prompt.append("\n[추천 액션]\n");
        if (actionPlans.isEmpty()) {
            prompt.append("- 추천 강의 후보 없음\n");
        } else {
            for (MyCompetencyEvaluationDto.ActionPlanDto actionPlan : actionPlans) {
                prompt.append("- ")
                        .append(actionPlan.getCourseTitle())
                        .append(" / ")
                        .append(actionPlan.getPrimarySkillName())
                        .append('\n');
            }
        }

        return prompt.toString();
    }

    private String extractMessageContent(String rawResponse) throws Exception {
        JsonNode root = objectMapper.readTree(rawResponse);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
            throw new IllegalStateException("AI competency summary response is empty.");
        }
        return normalizeJsonContent(contentNode.asText());
    }

    private String normalizeJsonContent(String content) {
        String normalizedContent = content.trim();
        if (normalizedContent.startsWith("```json")) {
            normalizedContent = normalizedContent.substring("```json".length()).trim();
        } else if (normalizedContent.startsWith("```")) {
            normalizedContent = normalizedContent.substring("```".length()).trim();
        }
        if (normalizedContent.endsWith("```")) {
            normalizedContent = normalizedContent.substring(0, normalizedContent.length() - "```".length()).trim();
        }
        return normalizedContent;
    }

    private MyCompetencyEvaluationDto.AiSummaryDto buildFallback(
            List<MyCompetencyEvaluationDto.CompetencyScoreDto> scores,
            List<MyCompetencyEvaluationDto.GrowthFactorDto> growthFactors,
            List<MyCompetencyEvaluationDto.ActionPlanDto> actionPlans
    ) {
        MyCompetencyEvaluationDto.CompetencyScoreDto strongest = scores.stream()
                .max(Comparator.comparingDouble(MyCompetencyEvaluationDto.CompetencyScoreDto::getUserScore))
                .orElse(null);
        MyCompetencyEvaluationDto.CompetencyScoreDto weakest = scores.stream()
                .min(Comparator.comparingDouble(MyCompetencyEvaluationDto.CompetencyScoreDto::getUserScore))
                .orElse(null);

        String weakestName = weakest == null ? "낮은 역량" : weakest.getCompetencyName();
        String strongestName = strongest == null ? "강점 역량" : strongest.getCompetencyName();

        String topGrowthFactor = growthFactors.isEmpty()
                ? null
                : growthFactors.get(0).getCalcTypeLabel();

        String firstActionTitle = actionPlans.isEmpty()
                ? null
                : actionPlans.get(0).getCourseTitle();

        String growthText = topGrowthFactor == null
                ? "최근 반영된 성장 이력은 아직 많지 않습니다. 현재 점수를 기준으로 부족한 역량을 하나씩 보완해가면 전체 균형을 만들 수 있습니다."
                : "최근에는 " + topGrowthFactor + " 활동이 역량 향상에 가장 크게 반영되었습니다. "
                  + strongestName + "은 안정적으로 유지되고 있으며, " + weakestName + "을 보완하면 전체 균형이 더 좋아집니다.";

        String nextAction = firstActionTitle == null
                ? weakestName + "과 연결된 학습을 1개 선택해 시작해보는 것을 추천합니다."
                : "'" + firstActionTitle + "' 학습을 먼저 진행해보는 것을 추천합니다.";

        String growthPotential = firstActionTitle == null
                ? weakestName + "을 중심으로 학습 이력이 쌓이면 종합 점수 개선 가능성이 있습니다."
                : "'" + firstActionTitle + "' 학습을 완료하면 " + weakestName + " 보완에 도움이 될 수 있습니다.";

        return MyCompetencyEvaluationDto.AiSummaryDto.builder()
                .headline(growthText)
                .strength(strongest == null
                        ? "현재 역량 점수 데이터를 수집 중입니다."
                        : "나의 가장 안정적인 강점은 "+ strongest.getCompetencyName() + " 입니다.")
                .improvement(weakest == null
                        ? "추가 학습 이력이 쌓이면 보완 방향이 더 선명해집니다."
                        : weakest.getCompetencyName() + "을 우선 보완하면 전체 균형이 좋아집니다.")
                .growthPotential(growthPotential)
                .nextAction(nextAction)
                .fallback(true)
                .build();
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private RestClient createRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(properties.getTimeoutSeconds()).toMillis());
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
