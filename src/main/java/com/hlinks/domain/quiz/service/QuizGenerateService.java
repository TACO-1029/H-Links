package com.hlinks.domain.quiz.service;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ai.service.AiQuizService;
import com.hlinks.domain.quiz.ai.service.CourseTranscriptIndexService;
import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizGenerateRequest;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizGenerateService {

    private final AiQuizService aiQuizService;
    private final QuizService quizService;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseTranscriptIndexService courseTranscriptIndexService;

    public List<QuizCreateRequest> generateAndSaveQuizzes(Long chapterId, int quizCount, String difficulty) {
        CourseChapter chapter = courseChapterMapper.findById(chapterId);

        if (chapter == null) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "존재하지 않는 챕터입니다. chapterId=" + chapterId);
        }

        String transcriptText = chapter.getTranscriptText();

        if (transcriptText == null || transcriptText.isBlank()) {
            throw new BaseException(
                    ErrorResponseCode.BAD_REQUEST,
                    "챕터 transcript가 비어 있어 퀴즈를 생성할 수 없습니다. chapterId=" + chapterId
            );
        }

        indexTranscriptSafely(chapter, transcriptText);

        QuizGenerateRequest request = new QuizGenerateRequest();
        request.setCourseId(chapter.getCourseId());
        request.setChapterId(chapter.getChapterId());
        request.setQuizCount(quizCount);
        request.setDifficulty(difficulty);
        request.setSourceText(transcriptText);

        List<QuizCreateRequest> quizzes = aiQuizService.generateQuizzes(request);

        quizService.saveQuizzes(quizzes);

        return quizzes;
    }

    private void indexTranscriptSafely(CourseChapter chapter, String transcriptText) {
        try {
            courseTranscriptIndexService.index(
                    chapter.getCourseId(),
                    chapter.getChapterId(),
                    resolveCourseTitle(chapter),
                    transcriptText
            );
        } catch (Exception e) {
            log.warn(
                    "강의 자막 Chroma 인덱싱 실패. courseId={}, chapterId={}, message={}",
                    chapter.getCourseId(),
                    chapter.getChapterId(),
                    e.getMessage()
            );
        }
    }

    private String resolveCourseTitle(CourseChapter chapter) {
        if (chapter.getChapterTitle() != null && !chapter.getChapterTitle().isBlank()) {
            return chapter.getChapterTitle();
        }

        return "course-" + chapter.getCourseId();
    }
}
