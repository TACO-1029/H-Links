package com.hlinks.domain.course.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.course.ai.AiCourseSummaryException;
import com.hlinks.domain.course.ai.AiCourseSummaryProperties;
import com.hlinks.domain.course.ai.CourseSummaryPromptBuilder;
import com.hlinks.domain.course.ai.dto.CourseSummaryGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCourseSummaryService {

    private final CourseSummaryPromptBuilder promptBuilder;
    private final AiCourseSummaryProperties properties;
    private final ObjectMapper objectMapper;

    public CourseSummaryGenerateResponse generateSummary(String transcriptText) {
        if (!StringUtils.hasText(transcriptText)) {
            throw new AiCourseSummaryException("강의 요약을 위한 transcriptText는 필수입니다.");
        }

        String prompt = promptBuilder.build(transcriptText);
        CourseSummaryGenerateResponse response = requestCourseSummaryGeneration(prompt);

        if (response == null || !StringUtils.hasText(response.getSummaryText())) {
            throw new AiCourseSummaryException("AI 강의 요약 결과가 비어 있습니다.");
        }

        if (response.getSkills() == null) {
            throw new AiCourseSummaryException("AI 강의 요약 skill 결과가 비어 있습니다.");
        }

        return response;
    }

    private CourseSummaryGenerateResponse requestCourseSummaryGeneration(String prompt) {
        validateApiKey();

        try {
            String rawResponse = createRestClient().post()
                    .uri(properties.getApiUrl())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createLlmRequestBody(prompt))
                    .retrieve()
                    .body(String.class);

            String content = extractMessageContent(rawResponse);

            return objectMapper.readValue(content, CourseSummaryGenerateResponse.class);
        } catch (AiCourseSummaryException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.warn("AI 강의 요약 응답 JSON 파싱 실패. provider={}, model={}", properties.getProvider(), properties.getModel());
            throw new AiCourseSummaryException("AI 강의 요약 응답 JSON 형식이 올바르지 않습니다.", e);
        } catch (Exception e) {
            log.warn("AI 강의 요약 요청 실패. provider={}, model={}", properties.getProvider(), properties.getModel());
            throw new AiCourseSummaryException("AI 강의 요약 요청에 실패했습니다.", e);
        }
    }

    private Map<String, Object> createLlmRequestBody(String prompt) {
        return Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "당신은 강의 transcript를 강의 추천, 스킬 매핑, 퀴즈 생성에 활용할 수 있는 메타데이터로 요약하는 도우미입니다. 반드시 유효한 JSON만 반환하세요."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );
    }

    private String extractMessageContent(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

            if (contentNode.isMissingNode() || !StringUtils.hasText(contentNode.asText())) {
                throw new AiCourseSummaryException("AI 강의 요약 응답이 비어 있습니다.");
            }

            return normalizeJsonContent(contentNode.asText());
        } catch (AiCourseSummaryException e) {
            throw e;
        } catch (Exception e) {
            throw new AiCourseSummaryException("AI 강의 요약 응답을 해석할 수 없습니다.", e);
        }
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

    private void validateApiKey() {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new AiCourseSummaryException("AI Course Summary API Key가 설정되어 있지 않습니다.");
        }
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
