package com.hlinks.domain.news.service;

import com.hlinks.domain.interest.dto.InterestDto;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.domain.news.dto.DashboardNewsResponse;
import com.hlinks.domain.news.dto.NewsArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private static final int DASHBOARD_NEWS_LIMIT = 4;
    private static final int MAX_INTEREST_QUERY_COUNT = 3;
    private static final int MAX_NEWS_QUERY_LENGTH = 450;
    private static final double SIMILAR_TITLE_THRESHOLD = 0.45;
    private static final String DEFAULT_KEYWORD = "\"generative AI\" OR \"software engineering\" OR cybersecurity";
    private static final List<String> TECH_INCLUDE_TERMS = List.of(
            "artificial intelligence",
            "generative ai",
            "machine learning",
            "large language model",
            "llm",
            "software engineering",
            "developer tools",
            "spring boot",
            "backend engineering",
            "java backend",
            "server-side development",
            "react framework",
            "frontend engineering",
            "typescript",
            "web development",
            "cybersecurity",
            "information security",
            "data breach",
            "zero-day vulnerability",
            "cloud infrastructure",
            "cloud computing",
            "devops",
            "kubernetes",
            "aws cloud",
            "ai transformation",
            "enterprise ai",
            "business automation"
    );
    private static final List<String> NON_TECH_EXCLUDE_TERMS = List.of(
            "sports",
            "baseball",
            "basketball",
            "football",
            "olympic",
            "fans",
            "concert",
            "festival",
            "celebrity",
            "crime",
            "police",
            "politics",
            "election",
            "mayor",
            "council",
            "housing",
            "construction",
            "real estate",
            "fireworks",
            "parade"
    );
    private static final Set<String> TITLE_STOP_WORDS = Set.of(
            "a",
            "an",
            "and",
            "are",
            "as",
            "at",
            "be",
            "by",
            "for",
            "from",
            "has",
            "in",
            "into",
            "is",
            "it",
            "its",
            "new",
            "of",
            "on",
            "or",
            "that",
            "the",
            "their",
            "this",
            "to",
            "with",
            "will"
    );

    private final InterestService interestService;
    private final NewsApiClient newsApiClient;
    private final TranslationApiClient translationApiClient;

    @Cacheable(cacheNames = "dashboardNews", key = "#userId == null ? 'anonymous' : #userId", unless = "#result == null || #result.isEmpty()")
    public List<DashboardNewsResponse> getPersonalizedNews(Long userId) {
        try {
            List<String> queries = buildInterestQueries(userId);
            String cacheKey = buildCacheKey(queries);
            log.debug("Load personalized news. userId={}, cacheKey={}, queryCount={}", userId, cacheKey, queries.size());

            List<NewsArticleDto> fetchedArticles = queries.stream()
                    .flatMap(query -> newsApiClient.fetchNews(query).stream())
                    .toList();
            List<NewsArticleDto> articles = selectDiverseArticles(fetchedArticles);
            if (articles.isEmpty() && !isDefaultQueries(queries)) {
                articles = selectDiverseArticles(newsApiClient.fetchNews(DEFAULT_KEYWORD));
            }

            return articles.stream()
                    .limit(DASHBOARD_NEWS_LIMIT)
                    .map(this::toDashboardResponse)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to load personalized dashboard news. userId={}", userId, e);
            return List.of();
        }
    }

    private List<String> buildInterestQueries(Long userId) {
        if (userId == null) {
            return List.of(DEFAULT_KEYWORD);
        }

        List<InterestDto> interests = interestService.getUserInterests(userId);
        if (interests == null || interests.isEmpty()) {
            return List.of(DEFAULT_KEYWORD);
        }

        List<String> queries = interests.stream()
                .map(InterestDto::getSkillName)
                .map(this::mapInterestToKeyword)
                .filter(StringUtils::hasText)
                .distinct()
                .map(this::truncateQuery)
                .limit(MAX_INTEREST_QUERY_COUNT)
                .toList();

        return queries.isEmpty() ? List.of(DEFAULT_KEYWORD) : queries;
    }

    private String mapInterestToKeyword(String interestName) {
        if (!StringUtils.hasText(interestName)) {
            return "";
        }

        String normalized = interestName.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains("AI")) {
            return "\"generative AI\" OR \"large language model\"";
        }
        if (normalized.contains("BACKEND") || normalized.contains("백엔드")) {
            return "\"Spring Boot\" OR \"backend engineering\"";
        }
        if (normalized.contains("FRONTEND") || normalized.contains("프론트")) {
            return "\"React framework\" OR TypeScript";
        }
        if (normalized.contains("SECURITY") || normalized.contains("보안")) {
            return "cybersecurity OR \"data breach\"";
        }
        if (normalized.contains("INFRA") || normalized.contains("인프라") || normalized.contains("CLOUD")) {
            return "Kubernetes OR DevOps";
        }
        if (normalized.contains("AX")) {
            return "\"enterprise AI\" OR \"AI transformation\"";
        }
        return "";
    }

    private List<NewsArticleDto> selectDiverseArticles(List<NewsArticleDto> articles) {
        if (articles == null || articles.isEmpty()) {
            return List.of();
        }

        List<NewsArticleDto> titleDedupedArticles = removeSimilarArticles(distinctByUrl(articles.stream()
                .filter(this::hasValidNewsContent)
                .filter(this::isLikelyTechArticle)
                .toList()));
        List<NewsArticleDto> sourceLimitedArticles = limitSameSource(titleDedupedArticles);

        return sourceLimitedArticles.size() >= DASHBOARD_NEWS_LIMIT ? sourceLimitedArticles : titleDedupedArticles;
    }

    private boolean hasValidNewsContent(NewsArticleDto article) {
        return article != null
                && StringUtils.hasText(article.getTitle())
                && StringUtils.hasText(article.getUrl());
    }

    private boolean isLikelyTechArticle(NewsArticleDto article) {
        if (article == null) {
            return false;
        }

        String searchableText = String.join(" ",
                defaultText(article.getTitle()),
                defaultText(article.getDescription())
        ).toLowerCase(Locale.ROOT);

        boolean hasTechSignal = TECH_INCLUDE_TERMS.stream().anyMatch(searchableText::contains);
        boolean hasNonTechSignal = NON_TECH_EXCLUDE_TERMS.stream().anyMatch(searchableText::contains);

        return hasTechSignal && !hasNonTechSignal;
    }

    private List<NewsArticleDto> distinctByUrl(List<NewsArticleDto> articles) {
        LinkedHashMap<String, NewsArticleDto> distinctArticles = new LinkedHashMap<>();
        for (NewsArticleDto article : articles) {
            String normalizedUrl = normalizeUrl(article.getUrl());
            if (StringUtils.hasText(normalizedUrl)) {
                distinctArticles.putIfAbsent(normalizedUrl, article);
            }
        }
        return new ArrayList<>(distinctArticles.values());
    }

    private List<NewsArticleDto> removeSimilarArticles(List<NewsArticleDto> articles) {
        List<NewsArticleDto> selectedArticles = new ArrayList<>();
        for (NewsArticleDto article : articles) {
            boolean alreadySelected = selectedArticles.stream()
                    .anyMatch(selectedArticle -> isSimilarTitle(selectedArticle.getTitle(), article.getTitle()));
            if (!alreadySelected) {
                selectedArticles.add(article);
            }
        }
        return selectedArticles;
    }

    private boolean isSimilarTitle(String title, String otherTitle) {
        Set<String> keywords = toKeywordSet(title);
        Set<String> otherKeywords = toKeywordSet(otherTitle);
        if (keywords.isEmpty() || otherKeywords.isEmpty()) {
            return false;
        }

        Set<String> intersection = new HashSet<>(keywords);
        intersection.retainAll(otherKeywords);

        Set<String> union = new HashSet<>(keywords);
        union.addAll(otherKeywords);

        return (double) intersection.size() / union.size() >= SIMILAR_TITLE_THRESHOLD;
    }

    private Set<String> toKeywordSet(String title) {
        if (!StringUtils.hasText(title)) {
            return Set.of();
        }

        return List.of(title.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9\\s]", " ")
                        .split("\\s+"))
                .stream()
                .filter(StringUtils::hasText)
                .filter(word -> word.length() > 1)
                .filter(word -> !TITLE_STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    private List<NewsArticleDto> limitSameSource(List<NewsArticleDto> articles) {
        Set<String> selectedSources = new HashSet<>();
        List<NewsArticleDto> selectedArticles = new ArrayList<>();
        for (NewsArticleDto article : articles) {
            String source = normalizeSource(article.getSource());
            if (selectedSources.add(source)) {
                selectedArticles.add(article);
            }
        }
        return selectedArticles;
    }

    private DashboardNewsResponse toDashboardResponse(NewsArticleDto article) {
        return DashboardNewsResponse.builder()
                .title(translationApiClient.translateToKorean(article.getTitle()))
                .description(translationApiClient.translateToKorean(article.getDescription()))
                .url(article.getUrl())
                .source(article.getSource())
                .publishedAt(article.getPublishedAt())
                .build();
    }

    private String buildCacheKey(List<String> queries) {
        String normalized = queries == null || queries.isEmpty()
                ? DEFAULT_KEYWORD.toLowerCase(Locale.ROOT)
                : queries.stream()
                .map(this::normalizeKeyword)
                .collect(Collectors.joining("|"));
        return "dashboard:news:" + Integer.toHexString(normalized.hashCode());
    }

    private boolean isDefaultQueries(List<String> queries) {
        return queries != null
                && queries.size() == 1
                && normalizeKeyword(DEFAULT_KEYWORD).equals(normalizeKeyword(queries.get(0)));
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.strip().toLowerCase(Locale.ROOT) : "";
    }

    private String truncateQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return "";
        }

        String trimmedQuery = query.trim();
        return trimmedQuery.length() <= MAX_NEWS_QUERY_LENGTH
                ? trimmedQuery
                : trimmedQuery.substring(0, MAX_NEWS_QUERY_LENGTH);
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        return url.trim().replaceAll("/+$", "").toLowerCase(Locale.ROOT);
    }

    private String normalizeSource(String source) {
        return StringUtils.hasText(source) ? source.trim().toLowerCase(Locale.ROOT) : "unknown";
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
