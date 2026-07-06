package com.hlinks.domain.news.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardNewsResponse {

    private String title;
    private String description;
    private String url;
    private String source;
    private String publishedAt;
}
