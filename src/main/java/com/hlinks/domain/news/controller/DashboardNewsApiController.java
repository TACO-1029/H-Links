package com.hlinks.domain.news.controller;

import com.hlinks.domain.news.dto.DashboardNewsResponse;
import com.hlinks.domain.news.service.NewsService;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardNewsApiController {

    private final NewsService newsService;

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
}
