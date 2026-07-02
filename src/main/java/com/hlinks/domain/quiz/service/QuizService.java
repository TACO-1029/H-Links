package com.hlinks.domain.quiz.service;

import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizListResponse;
import com.hlinks.domain.quiz.dto.QuizOptionCreateRequest;
import com.hlinks.domain.quiz.dto.QuizOptionResponse;
import com.hlinks.domain.quiz.dto.QuizResponse;
import com.hlinks.domain.quiz.entity.Quiz;
import com.hlinks.domain.quiz.entity.QuizOption;
import com.hlinks.domain.quiz.mapper.QuizMapper;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizMapper quizMapper;

    @Transactional(readOnly = true)
    public List<QuizListResponse> getQuizzesByCourseId(Long courseId) {
        if (courseId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "courseId는 필수입니다.");
        }

        return quizMapper.findByCourseId(courseId).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizListResponse> getQuizzesByChapterId(Long chapterId) {
        if (chapterId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "chapterId는 필수입니다.");
        }

        return quizMapper.findByChapterId(chapterId).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizResponse getQuiz(Long quizId) {
        if (quizId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "quizId는 필수입니다.");
        }

        Quiz quiz = quizMapper.findById(quizId);

        if (quiz == null) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "존재하지 않는 퀴즈입니다. quizId=" + quizId);
        }

        List<QuizOptionResponse> options = quizMapper.findOptionsByQuizId(quizId).stream()
                .map(this::toOptionResponse)
                .toList();

        return toResponse(quiz, options);
    }

    @Transactional
    public void saveQuizzes(List<QuizCreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "저장할 퀴즈가 없습니다.");
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
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "QUIZ 저장에 실패했습니다.");
        }

        Long quizId = quiz.getQuizId();

        if (quizId == null) {
            throw new BaseException(ErrorResponseCode.INTERNAL_SERVER_ERROR, "생성된 QUIZ_ID를 가져오지 못했습니다.");
        }

        for (QuizOptionCreateRequest optionRequest : request.getOptions()) {
            QuizOption option = toQuizOption(quizId, optionRequest);

            int optionInserted = quizMapper.insertQuizOption(option);

            if (optionInserted != 1) {
                throw new BaseException(
                        ErrorResponseCode.INTERNAL_SERVER_ERROR,
                        "QUIZ_OPTION 저장에 실패했습니다. quizId=" + quizId
                );
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
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "quiz request가 null입니다.");
        }

        if (request.getCourseId() == null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "courseId는 필수입니다.");
        }

        if (request.getChapterId() == null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "chapterId는 필수입니다.");
        }

        if (request.getQuestionText() == null || request.getQuestionText().isBlank()) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "questionText는 필수입니다.");
        }

        if (request.getQuestionType() == null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "questionType은 필수입니다.");
        }

        if (request.getOptions() == null || request.getOptions().size() != 4) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "객관식 보기는 정확히 4개여야 합니다.");
        }

        long correctCount = request.getOptions().stream()
                .filter(option -> "Y".equals(option.getCorrectYn()))
                .count();

        if (correctCount != 1) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "정답 보기는 정확히 1개여야 합니다.");
        }
    }

    private QuizListResponse toListResponse(Quiz quiz) {
        QuizListResponse response = new QuizListResponse();
        response.setQuizId(quiz.getQuizId());
        response.setCourseId(quiz.getCourseId());
        response.setChapterId(quiz.getChapterId());
        response.setQuestionType(quiz.getQuestionType());
        response.setQuestionText(quiz.getQuestionText());
        response.setDifficulty(quiz.getDifficulty());
        response.setStatus(quiz.getStatus());
        response.setAiGeneratedYn(quiz.getAiGeneratedYn());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());
        return response;
    }

    private QuizResponse toResponse(Quiz quiz, List<QuizOptionResponse> options) {
        QuizResponse response = new QuizResponse();
        response.setQuizId(quiz.getQuizId());
        response.setCourseId(quiz.getCourseId());
        response.setChapterId(quiz.getChapterId());
        response.setQuestionType(quiz.getQuestionType());
        response.setQuestionText(quiz.getQuestionText());
        response.setExplanation(quiz.getExplanation());
        response.setDifficulty(quiz.getDifficulty());
        response.setAnswerText(quiz.getAnswerText());
        response.setStatus(quiz.getStatus());
        response.setAiGeneratedYn(quiz.getAiGeneratedYn());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());
        response.setOptions(options);
        return response;
    }

    private QuizOptionResponse toOptionResponse(QuizOption option) {
        QuizOptionResponse response = new QuizOptionResponse();
        response.setOptionId(option.getOptionId());
        response.setQuizId(option.getQuizId());
        response.setOptionNo(option.getOptionNo());
        response.setOptionText(option.getOptionText());
        response.setCorrectYn(option.getCorrectYn());
        return response;
    }
}
