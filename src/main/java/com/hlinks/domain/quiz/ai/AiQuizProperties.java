package com.hlinks.domain.quiz.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.ai.quiz")
public class AiQuizProperties {

    private String provider = "openai";
    private String apiUrl = "https://api.openai.com/v1/chat/completions";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private double temperature = 0.2;
    private long timeoutSeconds = 120;
}
