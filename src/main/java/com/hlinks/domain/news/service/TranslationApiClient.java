package com.hlinks.domain.news.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TranslationApiClient {

    private final TranslationApiProperties properties;
    private final RestClient restClient;

    public TranslationApiClient(TranslationApiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .requestFactory(requestFactory(properties.getTimeoutSeconds()))
                .build();
    }

    public String translateToKorean(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        if (!StringUtils.hasText(properties.getApiUrl()) || !StringUtils.hasText(properties.getApiKey())) {
            return text;
        }

        try {
            TranslationResponse response = restClient.post()
                    .uri(properties.getApiUrl())
                    .header(HttpHeaders.AUTHORIZATION, "DeepL-Auth-Key " + properties.getApiKey())
                    .body(buildRequest(text))
                    .retrieve()
                    .body(TranslationResponse.class);

            if (response == null
                    || response.getTranslations() == null
                    || response.getTranslations().isEmpty()
                    || !StringUtils.hasText(response.getTranslations().get(0).getText())) {
                return text;
            }
            return response.getTranslations().get(0).getText();
        } catch (Exception e) {
            log.warn("Failed to translate news text. Use original text.", e);
            return text;
        }
    }

    private Map<String, Object> buildRequest(String text) {
        Map<String, Object> request = new HashMap<>();
        request.put("text", List.of(text));
        request.put("target_lang", properties.getTargetLang());
        return request;
    }

    private SimpleClientHttpRequestFactory requestFactory(long timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    @Getter
    @Setter
    public static class TranslationResponse {
        private List<TranslationItem> translations;
    }

    @Getter
    @Setter
    public static class TranslationItem {
        private String detectedSourceLanguage;
        private String text;
    }
}
