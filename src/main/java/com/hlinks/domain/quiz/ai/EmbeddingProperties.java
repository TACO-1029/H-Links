package com.hlinks.domain.quiz.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "hlinks.ai.embedding")
public class EmbeddingProperties {

    private String provider;

    private String apiUrl;

    private String apiKey;

    private String model;

    private Integer timeoutSeconds = 60;
}