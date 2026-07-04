package com.hlinks.domain.quiz.scheduler;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.service.QuizBuildService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizBuildScheduler {

    private static final int POLLING_LIMIT = 3;
    private final CourseChapterMapper courseChapterMapper;
    private final QuizBuildService quizBuildService;

    @Scheduled(fixedDelay = 60_000)
    public void processPendingQuizBuilds() {
        List<CourseChapter> pendingChapters = courseChapterMapper.findPendingQuizBuildChapters(POLLING_LIMIT);

        if (pendingChapters.isEmpty()) {
            return;
        }
        log.info("퀴즈 생성 대기 챕터 조회: {} 건", pendingChapters.size());

        for (CourseChapter chapter : pendingChapters) {
            try{
                quizBuildService.buildQuizForChapter(chapter.getChapterId());
            }catch (Exception e){
                log.error("퀴즈 생성 처리 중 예외 발생. chapterId={}", chapter.getChapterId(), e);
            }
        }
    }
}
