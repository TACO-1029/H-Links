package com.hlinks.domain.quiz.ai;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClient {

    private final EmbeddingProperties properties;
    private final RestClient.Builder restClientBuilder;

    public List<Double> embed(String input) {
        if (!StringUtils.hasText(input)) {
            throw new AiQuizException("임베딩할 텍스트가 비어 있습니다.");
        }

        validateProperties();

        RestClient restClient = restClientBuilder
                .requestFactory(createRequestFactory())
                .build();

        EmbeddingRequest request = new EmbeddingRequest(
                properties.getModel(),
                input
        );

        EmbeddingResponse response = restClient.post()
                .uri(properties.getApiUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                .body(request)
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new AiQuizException("OpenAI 임베딩 응답이 비어 있습니다.");
        }

        List<Double> embedding = response.getData().get(0).getEmbedding();

        if (embedding == null || embedding.isEmpty()) {
            throw new AiQuizException("OpenAI 임베딩 벡터가 비어 있습니다.");
        }

        return embedding;
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getApiUrl())) {
            throw new AiQuizException("hlinks.ai.embedding.api-url 설정이 필요합니다.");
        }

        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new AiQuizException("hlinks.ai.embedding.api-key 설정이 필요합니다.");
        }

        if (!StringUtils.hasText(properties.getModel())) {
            throw new AiQuizException("hlinks.ai.embedding.model 설정이 필요합니다.");
        }
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        int timeoutSeconds = properties.getTimeoutSeconds() == null
                ? 60
                : properties.getTimeoutSeconds();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        return factory;
    }

    @Getter
    @RequiredArgsConstructor
    private static class EmbeddingRequest {
        private final String model;
        private final String input;
    }

    @Getter
    @Setter
    private static class EmbeddingResponse {
        private List<EmbeddingData> data;
    }

    @Getter
    @Setter
    private static class EmbeddingData {
        private Integer index;
        private List<Double> embedding;
    }
}