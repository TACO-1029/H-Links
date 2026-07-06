package com.hlinks.domain.news.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.news.translation")
public class TranslationApiProperties {

    private String apiUrl;
    private String apiKey;
    private String targetLang = "KO";
    private long timeoutSeconds = 5;
}
