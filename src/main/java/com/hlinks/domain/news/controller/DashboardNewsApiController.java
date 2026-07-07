package com.hlinks.domain.news.controller;

import com.hlinks.domain.competency.service.CompetencyScoreService;
import com.hlinks.domain.news.dto.DashboardNewsResponse;
import com.hlinks.domain.news.service.NewsService;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardNewsApiController {

    private final NewsService newsService;
    private final CompetencyScoreService competencyScoreService;

    @GetMapping("/news")
    public ResponseEntity<List<DashboardNewsResponse>> getDashboardNews(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUserId() : null;

        try {
            return ResponseEntity.ok(newsService.getPersonalizedNews(userId));
        } catch (Exception e) {
            log.warn("Failed to load dashboard news. userId={}", userId, e);
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/news/click")
    public ResponseEntity<Void> clickDashboardNews(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("url") String url
    ) {
        URI redirectUri = toHttpUri(url);
        if (redirectUri == null) {
            return ResponseEntity.badRequest().build();
        }

        if (userDetails != null) {
            competencyScoreService.applyGlobalNewsClickScore(userDetails.getUserId(), redirectUri.toString());
        }

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, redirectUri.toString())
                .build();
    }

    private URI toHttpUri(String url) {
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return null;
            }
            return uri;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
