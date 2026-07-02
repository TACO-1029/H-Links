package com.hlinks.domain.quiz.service;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.course.mapper.CourseChapterMapper;
import com.hlinks.domain.quiz.ai.service.AiQuizService;
import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizGenerateRequest;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizGenerateService {

    private final AiQuizService aiQuizService;
    private final QuizService quizService;
    private final CourseChapterMapper courseChapterMapper;

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
}
