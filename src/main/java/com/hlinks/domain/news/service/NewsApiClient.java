package com.hlinks.domain.news.service;

import com.hlinks.domain.news.dto.NewsApiResponse;
import com.hlinks.domain.news.dto.NewsArticleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class NewsApiClient {

    private static final int MAX_QUERY_LENGTH = 450;

    private final NewsApiProperties properties;
    private final RestClient restClient;

    public NewsApiClient(NewsApiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory(properties.getTimeoutSeconds()))
                .build();
    }

    public List<NewsArticleDto> fetchNews(String keyword) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            log.warn("News API key is not configured. Skip personalized news request.");
            return List.of();
        }
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }

        String query = keyword.trim();
        if (query.length() > MAX_QUERY_LENGTH) {
            log.warn("News API query is too long. length={}, max={}", query.length(), MAX_QUERY_LENGTH);
            return List.of();
        }

        try {
            NewsApiResponse response = restClient.get()
                    .uri(properties.getApiUrl(), builder -> builder
                            .queryParam("q", query)
                            .queryParam("searchIn", "title,description")
                            .queryParam("language", "en")
                            .queryParam("sortBy", "publishedAt")
                            .queryParam("pageSize", requestPageSize())
                            .build())
                    .header("X-Api-Key", properties.getApiKey())
                    .retrieve()
                    .body(NewsApiResponse.class);

            if (response == null || response.getArticles() == null) {
                return List.of();
            }

            return response.getArticles().stream()
                    .filter(article -> article != null && StringUtils.hasText(article.getTitle()))
                    .map(this::toArticleDto)
                    .limit(properties.getPageSize())
                    .toList();
        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("News API rejected query. status={}, length={}, query={}",
                    e.getStatusCode(), query.length(), query, e);
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to fetch personalized news. keyword={}", query, e);
            return List.of();
        }
    }

    private NewsArticleDto toArticleDto(NewsApiResponse.Article article) {
        return NewsArticleDto.builder()
                .title(defaultText(article.getTitle()))
                .description(defaultText(article.getDescription()))
                .url(defaultText(article.getUrl()))
                .urlToImage(defaultText(article.getUrlToImage()))
                .source(article.getSource() != null ? defaultText(article.getSource().getName()) : "")
                .publishedAt(defaultText(article.getPublishedAt()))
                .build();
    }

    private int requestPageSize() {
        int pageSize = Math.max(1, properties.getPageSize());
        return Math.min(20, pageSize);
    }

    private SimpleClientHttpRequestFactory requestFactory(long timeoutSeconds) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return factory;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
