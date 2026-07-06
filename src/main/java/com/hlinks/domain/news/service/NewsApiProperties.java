package com.hlinks.domain.news.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "hlinks.news.news-api")
public class NewsApiProperties {

    private String apiUrl = "https://newsapi.org/v2/everything";
    private String apiKey;
    private int pageSize = 4;
    private long timeoutSeconds = 5;
}
