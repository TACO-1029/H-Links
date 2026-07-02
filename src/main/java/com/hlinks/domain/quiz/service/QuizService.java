package com.hlinks.domain.quiz.service;

import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizOptionCreateRequest;
import com.hlinks.domain.quiz.entity.Quiz;
import com.hlinks.domain.quiz.entity.QuizOption;
import com.hlinks.domain.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizMapper quizMapper;

    @Transactional
    public void saveQuizzes(List<QuizCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("저장할 퀴즈가 없습니다.");
        }

        for (QuizCreateRequest request : requests) {
            saveQuiz(request);
        }
    }

    private void saveQuiz(QuizCreateRequest request) {
        validateQuiz(request);

        Quiz quiz = toQuiz(request);

        int quizInserted = quizMapper.insertQuiz(quiz);

        if (quizInserted != 1) {
            throw new IllegalStateException("QUIZ 저장에 실패했습니다.");
        }

        Long quizId = quiz.getQuizId();

        if (quizId == null) {
            throw new IllegalStateException("생성된 QUIZ_ID를 가져오지 못했습니다.");
        }

        for (QuizOptionCreateRequest optionRequest : request.getOptions()) {
            QuizOption option = toQuizOption(quizId, optionRequest);

            int optionInserted = quizMapper.insertQuizOption(option);

            if (optionInserted != 1) {
                throw new IllegalStateException("QUIZ_OPTION 저장에 실패했습니다. quizId=" + quizId);
            }
        }
    }

    private Quiz toQuiz(QuizCreateRequest request) {
        Quiz quiz = new Quiz();
        quiz.setCourseId(request.getCourseId());
        quiz.setChapterId(request.getChapterId());
        quiz.setQuestionType(request.getQuestionType().name());
        quiz.setQuestionText(request.getQuestionText());
        quiz.setAnswerText(request.getAnswerText());
        quiz.setExplanation(request.getExplanation());
        quiz.setDifficulty(request.getDifficulty());
        quiz.setStatus(request.getStatus());
        quiz.setAiGeneratedYn(request.getAiGeneratedYn());

        return quiz;
    }

    private QuizOption toQuizOption(Long quizId, QuizOptionCreateRequest request) {
        QuizOption option = new QuizOption();
        option.setQuizId(quizId);
        option.setOptionNo(request.getOptionNo());
        option.setOptionText(request.getOptionText());
        option.setCorrectYn(request.getCorrectYn());

        return option;
    }

    private void validateQuiz(QuizCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("quiz request가 null입니다.");
        }

        if (request.getCourseId() == null) {
            throw new IllegalArgumentException("courseId는 필수입니다.");
        }

        if (request.getChapterId() == null) {
            throw new IllegalArgumentException("chapterId는 필수입니다.");
        }

        if (request.getQuestionText() == null || request.getQuestionText().isBlank()) {
            throw new IllegalArgumentException("questionText는 필수입니다.");
        }

        if (request.getQuestionType() == null) {
            throw new IllegalArgumentException("questionType은 필수입니다.");
        }

        if (request.getOptions() == null || request.getOptions().size() != 4) {
            throw new IllegalArgumentException("객관식 보기는 정확히 4개여야 합니다.");
        }

        long correctCount = request.getOptions().stream()
                .filter(option -> "Y".equals(option.getCorrectYn()))
                .count();

        if (correctCount != 1) {
            throw new IllegalArgumentException("정답 보기는 정확히 1개여야 합니다.");
        }
    }
}
