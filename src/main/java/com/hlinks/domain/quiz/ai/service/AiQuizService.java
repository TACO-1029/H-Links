package com.hlinks.domain.quiz.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlinks.domain.quiz.ai.AiQuizException;
import com.hlinks.domain.quiz.ai.AiQuizProperties;
import com.hlinks.domain.quiz.ai.QuizPromptBuilder;
import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizGenerateRequest;
import com.hlinks.domain.quiz.dto.QuizOptionCreateRequest;
import com.hlinks.domain.quiz.ai.dto.AiGeneratedQuiz;
import com.hlinks.domain.quiz.ai.dto.AiGeneratedQuizOption;
import com.hlinks.domain.quiz.ai.dto.AiQuizGenerateResponse;
import com.hlinks.domain.quiz.type.QuestionType;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
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
public class AiQuizService {

    private static final int DEFAULT_QUIZ_COUNT = 3;
    private static final int MAX_QUIZ_COUNT = 10;
    private static final String DEFAULT_DIFFICULTY = "MEDIUM";
    private static final String DRAFT_STATUS = "DRAFT";
    private static final String AI_GENERATED_YN = "Y";

    private final QuizPromptBuilder quizPromptBuilder;
    private final AiQuizProperties properties;
    private final ObjectMapper objectMapper;

    public List<QuizCreateRequest> generateQuizzes(QuizGenerateRequest request) {
        validate(request);

        int quizCount = resolveQuizCount(request.getQuizCount());
        String difficulty = resolveDifficulty(request.getDifficulty());

        String prompt = quizPromptBuilder.build(request.getSourceText(), quizCount, difficulty);
        AiQuizGenerateResponse aiResponse = requestAiQuizGeneration(prompt);
        validateAiResponse(aiResponse);

        return aiResponse.getQuizzes().stream()
                .map(aiQuiz -> toCreateRequest(request, aiQuiz))
                .toList();
    }

    private void validate(QuizGenerateRequest request) {
        if (request == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_BODY);
        }

        if (request.getCourseId() == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_BODY);
        }

        if (!StringUtils.hasText(request.getSourceText())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_BODY);
        }
    }

    private int resolveQuizCount(Integer quizCount) {
        if (quizCount == null) {
            return DEFAULT_QUIZ_COUNT;
        }

        if (quizCount < 1 || quizCount > MAX_QUIZ_COUNT) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_BODY);
        }

        return quizCount;
    }

    private String resolveDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return DEFAULT_DIFFICULTY;
        }

        return difficulty.trim().toUpperCase();
    }

    private AiQuizGenerateResponse requestAiQuizGeneration(String prompt) {
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

            return objectMapper.readValue(content, AiQuizGenerateResponse.class);
        } catch (AiQuizException e) {
            throw e;
        } catch (JsonProcessingException e) {
            log.warn("AI 퀴즈 생성 응답 JSON 파싱 실패. provider={}, model={}", properties.getProvider(), properties.getModel());
            throw new AiQuizException("AI 퀴즈 생성 응답 JSON 형식이 올바르지 않습니다.", e);
        } catch (Exception e) {
            log.warn("AI 퀴즈 생성 요청 실패. provider={}, model={}", properties.getProvider(), properties.getModel());
            throw new AiQuizException("AI 퀴즈 생성 요청에 실패했습니다.", e);
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
                                "content", "당신은 강의 내용을 기반으로 학습용 객관식 퀴즈를 생성하는 도우미입니다. 반드시 유효한 JSON만 반환하세요."
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
                throw new AiQuizException("AI 퀴즈 생성 응답이 비어 있습니다.");
            }

            return normalizeJsonContent(contentNode.asText());
        } catch (AiQuizException e) {
            throw e;
        } catch (Exception e) {
            throw new AiQuizException("AI 퀴즈 생성 응답을 해석할 수 없습니다.", e);
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
            throw new AiQuizException("AI Quiz API Key가 설정되어 있지 않습니다.");
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

    private void validateAiResponse(AiQuizGenerateResponse aiResponse) {
        if (aiResponse == null || aiResponse.getQuizzes() == null || aiResponse.getQuizzes().isEmpty()) {
            throw new AiQuizException("AI 퀴즈 생성 결과가 비어 있습니다.");
        }
    }

    private QuizCreateRequest toCreateRequest(QuizGenerateRequest request, AiGeneratedQuiz aiQuiz) {
        validateAiQuiz(aiQuiz);

        QuizCreateRequest quizCreateRequest = new QuizCreateRequest();
        quizCreateRequest.setCourseId(request.getCourseId());
        quizCreateRequest.setChapterId(request.getChapterId());
        quizCreateRequest.setQuestionType(QuestionType.MULTIPLE_CHOICE);
        quizCreateRequest.setQuestionText(aiQuiz.getQuestionText());
        quizCreateRequest.setExplanation(aiQuiz.getExplanation());
        quizCreateRequest.setDifficulty(aiQuiz.getDifficulty());
        quizCreateRequest.setAnswerText(aiQuiz.getAnswerText());
        quizCreateRequest.setStatus(DRAFT_STATUS);
        quizCreateRequest.setAiGeneratedYn(AI_GENERATED_YN);
        quizCreateRequest.setOptions(toOptionCreateRequests(aiQuiz.getOptions()));
        return quizCreateRequest;
    }

    private List<QuizOptionCreateRequest> toOptionCreateRequests(List<AiGeneratedQuizOption> aiOptions) {
        return aiOptions.stream()
                .map(this::toOptionCreateRequest)
                .toList();
    }

    private QuizOptionCreateRequest toOptionCreateRequest(AiGeneratedQuizOption aiOption) {
        QuizOptionCreateRequest optionCreateRequest = new QuizOptionCreateRequest();
        optionCreateRequest.setOptionNo(String.valueOf(aiOption.getOptionNo()));
        optionCreateRequest.setOptionText(aiOption.getOptionText());
        optionCreateRequest.setCorrectYn(aiOption.getCorrectYn());
        return optionCreateRequest;
    }

    private void validateAiQuiz(AiGeneratedQuiz aiQuiz) {
        if (aiQuiz == null) {
            throw new AiQuizException("AI 퀴즈 생성 결과에 빈 퀴즈가 포함되어 있습니다.");
        }

        List<AiGeneratedQuizOption> options = aiQuiz.getOptions();

        if (options == null || options.size() != 4) {
            throw new AiQuizException("AI 퀴즈 선택지는 정확히 4개여야 합니다.");
        }

        long correctCount = options.stream()
                .filter(option -> option != null && "Y".equals(option.getCorrectYn()))
                .count();

        if (correctCount != 1) {
            throw new AiQuizException("AI 퀴즈 정답 선택지는 정확히 1개여야 합니다.");
        }

        for (AiGeneratedQuizOption option : options) {
            validateAiQuizOption(option);
        }
    }

    private void validateAiQuizOption(AiGeneratedQuizOption option) {
        if (option == null) {
            throw new AiQuizException("AI 퀴즈 선택지가 비어 있습니다.");
        }

        if (option.getOptionNo() == null) {
            throw new AiQuizException("AI 퀴즈 선택지 번호는 필수입니다.");
        }

        if (!StringUtils.hasText(option.getOptionText())) {
            throw new AiQuizException("AI 퀴즈 선택지 내용은 필수입니다.");
        }

        if (!"Y".equals(option.getCorrectYn()) && !"N".equals(option.getCorrectYn())) {
            throw new AiQuizException("AI 퀴즈 선택지 정답 여부는 Y 또는 N만 허용됩니다.");
        }
    }
}
