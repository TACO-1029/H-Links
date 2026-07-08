package com.hlinks.domain.mypage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompetencyEvaluationWarmupService {

    private final MyCompetencyEvaluationService myCompetencyEvaluationService;

    @Async("competencyAiWarmupExecutor")
    public void warmup(Long userId) {
        try {
            log.debug("Start competency evaluation cache warmup. userId={}", userId);
            myCompetencyEvaluationService.getEvaluation(userId);
            log.debug("Complete competency evaluation cache warmup. userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to warm up competency evaluation cache. userId={}", userId, e);
        }
    }
}
