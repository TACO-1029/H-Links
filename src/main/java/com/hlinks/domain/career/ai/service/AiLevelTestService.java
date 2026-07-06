package com.hlinks.domain.career.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.career.ai.LevelTestPromptBuilder;
import com.hlinks.domain.career.ai.dto.AiGeneratedLevelTestOption;
import com.hlinks.domain.career.ai.dto.AiGeneratedLevelTestQuestion;
import com.hlinks.domain.career.ai.dto.AiLevelTestGenerateResponse;
import com.hlinks.domain.career.entity.LevelTestOption;
import com.hlinks.domain.career.entity.LevelTestQuestion;
import com.hlinks.domain.career.exception.CareerErrorCode;
import com.hlinks.domain.career.mapper.CareerMapper;
import com.hlinks.domain.quiz.ai.AiQuizProperties;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiLevelTestService {

    private final LevelTestPromptBuilder promptBuilder;
    private final AiQuizProperties properties; // Reusing teammate's AI properties
    private final CareerMapper careerMapper;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    @Async
    public void buildLevelTestAsync(Long diagnosisId, List<Long> skillIds, List<String> difficulties) {
        log.info("Starting level test generation in background. DiagnosisId: {}, Skills: {}", diagnosisId, skillIds);
        transactionTemplate.executeWithoutResult(status -> 
            careerMapper.updateLevelTestBuildStatus(diagnosisId, "PROCESSING")
        );

        try {
            int skillCount = skillIds.size();

            for (int i = 0; i < skillCount; i++) {
                Long skillId = skillIds.get(i);
                String difficulty = (difficulties != null && difficulties.size() > i) ? difficulties.get(i) : "중";
                String skillName = careerMapper.getSkillNameById(skillId);
                if (skillName == null) {
                    skillName = "Unknown Skill";
                }

                // Determine question ratios based on user difficulty choice and total questions
                int lowCount = 0, mediumCount = 0, highCount = 0;
                if (skillCount == 1) {
                    if ("하".equals(difficulty)) {
                        lowCount = 8; mediumCount = 2; highCount = 0;
                    } else if ("상".equals(difficulty)) {
                        lowCount = 1; mediumCount = 3; highCount = 6;
                    } else { // "중"
                        lowCount = 2; mediumCount = 6; highCount = 2;
                    }
                } else if (skillCount == 2) {
                    if ("하".equals(difficulty)) {
                        lowCount = 5; mediumCount = 1; highCount = 0;
                    } else if ("상".equals(difficulty)) {
                        lowCount = 1; mediumCount = 1; highCount = 4;
                    } else { // "중"
                        lowCount = 1; mediumCount = 4; highCount = 1;
                    }
                } else if (skillCount == 3) { // 5문항
                    if ("하".equals(difficulty)) {
                        lowCount = 4; mediumCount = 1; highCount = 0;
                    } else if ("상".equals(difficulty)) {
                        lowCount = 0; mediumCount = 2; highCount = 3;
                    } else { // "중"
                        lowCount = 1; mediumCount = 3; highCount = 1;
                    }
                } else {
                    throw new BaseException(CareerErrorCode.INVALID_SKILL_COUNT);
                }

                String prompt = promptBuilder.build(skillName, lowCount + mediumCount + highCount, lowCount, mediumCount, highCount);
                
                // HTTP API Call to OpenAI is executed outside the transaction boundary
                AiLevelTestGenerateResponse response = requestAiLevelTestGeneration(prompt);

                // Question validation and DB insertion is committed in a short, separate transaction per skill
                final int expectedCount = lowCount + mediumCount + highCount;
                transactionTemplate.executeWithoutResult(status -> 
                    validateAndSaveQuestions(diagnosisId, skillId, response, expectedCount)
                );
            }

            transactionTemplate.executeWithoutResult(status -> 
                careerMapper.updateLevelTestBuildStatus(diagnosisId, "COMPLETED")
            );
            log.info("Level test generation completed successfully for DiagnosisId: {}", diagnosisId);

        } catch (Exception e) {
            log.error("Failed to build level test for DiagnosisId: {}", diagnosisId, e);
            transactionTemplate.executeWithoutResult(status -> {
                careerMapper.updateLevelTestBuildStatus(diagnosisId, "FAILED");
                careerMapper.updateLlmSummary(diagnosisId, "AI 레벨 테스트 생성 도중 오류가 발생했습니다: " + e.getMessage());
            });
        }
    }

    private AiLevelTestGenerateResponse requestAiLevelTestGeneration(String prompt) throws JsonProcessingException {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("AI Quiz API Key가 설정되어 있지 않습니다.");
        }

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = Math.toIntExact(Duration.ofSeconds(properties.getTimeoutSeconds()).toMillis());
        requestFactory.setConnectTimeout(timeoutMillis);
        requestFactory.setReadTimeout(timeoutMillis);

        RestClient restClient = RestClient.builder().requestFactory(requestFactory).build();

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "temperature", properties.getTemperature(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "당신은 IT 스킬 평가용 레벨테스트 문제를 생성하는 도우미입니다. 반드시 제공된 템플릿 형태의 유효한 JSON만 반환해야 합니다."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        String rawResponse = restClient.post()
                .uri(properties.getApiUrl())
                .header("Authorization", "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(rawResponse);
        String content = root.path("choices").path(0).path("message").path("content").asText();

        // Clean markdown fences
        content = content.trim();
        if (content.startsWith("```json")) {
            content = content.substring("```json".length()).trim();
        } else if (content.startsWith("```")) {
            content = content.substring("```".length()).trim();
        }
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - "```".length()).trim();
        }

        return objectMapper.readValue(content, AiLevelTestGenerateResponse.class);
    }

    private void validateAndSaveQuestions(Long diagnosisId, Long skillId, AiLevelTestGenerateResponse response, int expectedCount) {
        if (response == null || response.getQuestions() == null || response.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("AI 생성 질문 데이터가 비어 있습니다.");
        }

        List<AiGeneratedLevelTestQuestion> genQuestions = response.getQuestions();
        if (genQuestions.size() != expectedCount) {
            log.warn("Generated question count mismatch. Expected: {}, Actual: {}", expectedCount, genQuestions.size());
        }

        for (AiGeneratedLevelTestQuestion genQ : genQuestions) {
            // Validate question
            if (!StringUtils.hasText(genQ.getQuestionText())) {
                throw new IllegalArgumentException("질문 문구는 필수입니다.");
            }
            if (genQ.getOptions() == null || genQ.getOptions().size() != 4) {
                throw new IllegalArgumentException("보기 옵션은 정확히 4개여야 합니다.");
            }

            long correctCount = genQ.getOptions().stream()
                    .filter(opt -> opt != null && "Y".equals(opt.getCorrectYn()))
                    .count();
            if (correctCount != 1) {
                throw new IllegalArgumentException("정답 보기는 정확히 1개여야 합니다.");
            }

            // Save question
            LevelTestQuestion question = new LevelTestQuestion();
            question.setDiagnosisId(diagnosisId);
            question.setSkillId(skillId);
            question.setQuestionText(genQ.getQuestionText());
            question.setQuestionType("MULTIPLE_CHOICE");
            question.setDifficulty(genQ.getDifficulty() != null ? genQ.getDifficulty().toUpperCase() : "MEDIUM");
            question.setExplanation(genQ.getExplanation());
            
            careerMapper.insertLevelTestQuestion(question);

            // Save options (shuffled to randomize option number)
            List<AiGeneratedLevelTestOption> options = new java.util.ArrayList<>(genQ.getOptions());
            java.util.Collections.shuffle(options);
            int optNo = 1;
            for (AiGeneratedLevelTestOption genOpt : options) {
                LevelTestOption option = new LevelTestOption();
                option.setLevelQuestionId(question.getLevelQuestionId());
                option.setOptionNo(String.valueOf(optNo++));
                option.setOptionText(genOpt.getOptionText());
                option.setCorrectYn(genOpt.getCorrectYn() != null ? genOpt.getCorrectYn() : "N");
                
                careerMapper.insertLevelTestOption(option);
            }
        }
    }

    public String generateFeedbackSummary(String categoryName, String scoreInfo) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("AI Quiz API Key is missing. Falling back to default feedback.");
            return "테스트 채점이 완료되었습니다. 선택하신 기술 분야를 바탕으로 기본 개념 학습을 다지고 실무 프로젝트 실습을 진행해 보세요!";
        }

        try {
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            int timeoutMillis = Math.toIntExact(Duration.ofSeconds(properties.getTimeoutSeconds()).toMillis());
            requestFactory.setConnectTimeout(timeoutMillis);
            requestFactory.setReadTimeout(timeoutMillis);

            RestClient restClient = RestClient.builder().requestFactory(requestFactory).build();

            String systemPrompt = "당신은 IT 스킬 평가 전문가이자 멘토입니다. 학습자의 점수 결과를 바탕으로 진심 어린 피드백을 한국어로 제공해야 합니다.";
            String userPrompt = String.format(
                    "학습자가 '%s' 기술 분야에서 치른 레벨 테스트 결과 정보는 다음과 같습니다:\n" +
                    "%s\n\n" +
                    "이 결과를 바탕으로 학습자의 현재 역량 상태를 진단하고, 앞으로 어떤 부분을 공부해야 하는지 조언하는 'AI 진단 총평'을 3~4문장의 깔끔한 줄글 단락으로 작성해 주세요.\n" +
                    "[조건]\n" +
                    "- 공손하고 격려하는 어조를 사용해 주세요.\n" +
                    "- 마크다운, 따옴표, 또는 JSON 형식을 절대 쓰지 말고 오직 순수한 텍스트 단락만 반환해 주세요.",
                    categoryName, scoreInfo
            );

            Map<String, Object> requestBody = Map.of(
                    "model", properties.getModel(),
                    "temperature", 0.5,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    )
            );

            String rawResponse = restClient.post()
                    .uri(properties.getApiUrl())
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("choices").path(0).path("message").path("content").asText().trim();
        } catch (Exception e) {
            log.error("Failed to generate AI feedback summary via LLM", e);
            return "테스트 채점이 완료되었습니다. 선택하신 기술 분야를 바탕으로 기본 개념 학습을 다지고 실무 프로젝트 실습을 진행해 보세요!";
        }
    }
}
