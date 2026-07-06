package com.hlinks.domain.news.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NewsApiResponse {

    private String status;
    private Integer totalResults;
    private List<Article> articles;

    @Getter
    @Setter
    public static class Article {
        private Source source;
        private String title;
        private String description;
        private String url;
        private String urlToImage;
        private String publishedAt;
    }

    @Getter
    @Setter
    public static class Source {
        private String name;
    }
}
