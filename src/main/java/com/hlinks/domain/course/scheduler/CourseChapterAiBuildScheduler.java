package com.hlinks.domain.course.scheduler;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.course.service.CourseChapterAiBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseChapterAiBuildScheduler {

    private static final int POLLING_LIMIT = 3;

    private final CourseChapterMapper courseChapterMapper;
    private final CourseChapterAiBuildService courseChapterAiBuildService;

    @Scheduled(fixedDelay = 60_000)
    public void processPendingCourseChapterAiBuilds() {
        List<CourseChapter> pendingChapters = courseChapterMapper.findPendingQuizBuildChapters(POLLING_LIMIT);

        if (pendingChapters.isEmpty()) {
            return;
        }

        log.info("AI 처리 대기 챕터 조회: {} 건", pendingChapters.size());

        for (CourseChapter chapter : pendingChapters) {
            try {
                courseChapterAiBuildService.buildForChapter(chapter.getChapterId());
            } catch (Exception e) {
                log.error("챕터 AI 처리 중 예외 발생. chapterId={}", chapter.getChapterId(), e);
            }
        }
    }
}
