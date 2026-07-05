package com.hlinks.domain.quiz.service;

import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.domain.course.type.LearningStatus;
import com.hlinks.domain.quiz.dto.ChapterQuizOptionResponse;
import com.hlinks.domain.quiz.dto.ChapterQuizPageResponse;
import com.hlinks.domain.quiz.dto.ChapterQuizQuestionResponse;
import com.hlinks.domain.quiz.dto.ChapterQuizResultItem;
import com.hlinks.domain.quiz.dto.ChapterQuizResultResponse;
import com.hlinks.domain.quiz.dto.QuizAnswerResultRow;
import com.hlinks.domain.quiz.dto.QuizAttemptResultRow;
import com.hlinks.domain.quiz.dto.QuizAttemptTargetDto;
import com.hlinks.domain.quiz.dto.WrongAnswerNoteResponse;
import com.hlinks.domain.quiz.entity.Quiz;
import com.hlinks.domain.quiz.entity.QuizOption;
import com.hlinks.domain.quiz.mapper.QuizMapper;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizAttemptService {

    private static final int MIN_QUIZ_PROGRESS_RATE = 95;
    private static final String CORRECT_YN = "Y";
    private static final String WRONG_YN = "N";

    private final QuizMapper quizMapper;
    private final CourseMapper courseMapper;

    public ChapterQuizPageResponse getChapterQuizPage(Long courseId, Long chapterId, Long userId) {
        QuizAttemptTargetDto target = validateAttemptTarget(courseId, chapterId, userId);
        validateProgress(target);
        validateNotSubmitted(courseId, chapterId, userId);

        List<ChapterQuizQuestionResponse> questions = findAttemptQuestions(chapterId);
        return ChapterQuizPageResponse.builder()
                .courseId(target.getCourseId())
                .chapterId(target.getChapterId())
                .courseTitle(target.getCourseTitle())
                .chapterTitle(target.getChapterTitle())
                .progressRate(defaultNumber(target.getProgressRate()))
                .questions(questions)
                .build();
    }

    @Transactional
    public Long submitChapterQuiz(Long courseId, Long chapterId, Long userId, Map<Long, Long> selectedOptionIdsByQuizId) {
        QuizAttemptTargetDto target = validateAttemptTarget(courseId, chapterId, userId);
        validateProgress(target);
        validateNotSubmitted(courseId, chapterId, userId);

        List<Quiz> quizzes = findChapterQuizzes(chapterId);
        validateSubmittedAnswers(quizzes, selectedOptionIdsByQuizId);

        Long attemptId = quizMapper.nextQuizAttemptId();
        quizMapper.insertQuizAttempt(attemptId, userId, courseId, chapterId);

        int correctCount = 0;
        for (Quiz quiz : quizzes) {
            Long selectedOptionId = selectedOptionIdsByQuizId.get(quiz.getQuizId());
            QuizOption correctOption = findCorrectOption(quiz.getQuizId());
            boolean correct = correctOption != null && Objects.equals(correctOption.getOptionId(), selectedOptionId);
            String correctYn = correct ? CORRECT_YN : WRONG_YN;

            quizMapper.insertQuizAttemptAnswer(attemptId, quiz.getQuizId(), selectedOptionId, correctYn);
            if (correct) {
                correctCount++;
            }
        }

        courseMapper.completeChapterLearningByQuizAttempt(
                target.getChapterLearningId(),
                attemptId,
                LearningStatus.COMPLETED.name()
        );
        courseMapper.updateCourseLearningProgress(target.getCourseLearningId(), courseId);
        courseMapper.completeCourseLearningIfAllChaptersCompleted(
                target.getCourseLearningId(),
                courseId,
                LearningStatus.COMPLETED.name()
        );

        return attemptId;
    }

    public ChapterQuizResultResponse getAttemptResult(Long attemptId, Long userId) {
        Long ownerUserId = quizMapper.findAttemptOwnerUserId(attemptId);
        if (ownerUserId == null) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "존재하지 않는 퀴즈 응시 결과입니다.");
        }
        if (!ownerUserId.equals(userId)) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN, "본인의 퀴즈 응시 결과만 조회할 수 있습니다.");
        }

        List<QuizAttemptResultRow> rows = quizMapper.findAttemptResult(attemptId);
        if (rows.isEmpty()) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "존재하지 않는 퀴즈 응시 결과입니다.");
        }

        QuizAttemptResultRow first = rows.get(0);
        List<ChapterQuizResultItem> items = rows.stream()
                .map(this::toResultItem)
                .toList();
        int correctCount = (int) rows.stream()
                .filter(row -> CORRECT_YN.equals(row.getCorrectYn()))
                .count();

        return ChapterQuizResultResponse.builder()
                .attemptId(first.getAttemptId())
                .courseId(first.getCourseId())
                .chapterId(first.getChapterId())
                .courseTitle(first.getCourseTitle())
                .chapterTitle(first.getChapterTitle())
                .submittedAt(first.getSubmittedAt())
                .totalCount(rows.size())
                .correctCount(correctCount)
                .items(items)
                .build();
    }

    public List<WrongAnswerNoteResponse> getWrongAnswerNotes(Long userId, Long courseId, Long chapterId) {
        return quizMapper.findWrongAnswerNotes(userId, courseId, chapterId);
    }

    private QuizAttemptTargetDto validateAttemptTarget(Long courseId, Long chapterId, Long userId) {
        if (courseId == null || chapterId == null || userId == null) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
        }

        QuizAttemptTargetDto target = quizMapper.findQuizAttemptTarget(userId, courseId, chapterId);
        if (target == null) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN, "신청한 온라인 강의의 챕터 퀴즈만 응시할 수 있습니다.");
        }
        return target;
    }

    private void validateProgress(QuizAttemptTargetDto target) {
        if (defaultNumber(target.getProgressRate()) < MIN_QUIZ_PROGRESS_RATE) {
            throw new BaseException(
                    ErrorResponseCode.FORBIDDEN,
                    "진도율 95% 이상 수강 후 퀴즈에 응시할 수 있습니다."
            );
        }
    }

    private void validateNotSubmitted(Long courseId, Long chapterId, Long userId) {
        Long attemptId = quizMapper.findAttemptIdByUserAndChapter(userId, courseId, chapterId);
        if (attemptId != null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "이미 제출한 챕터 퀴즈입니다.");
        }
    }

    private List<Quiz> findChapterQuizzes(Long chapterId) {
        List<Quiz> quizzes = quizMapper.findByChapterId(chapterId);
        if (quizzes == null || quizzes.isEmpty()) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "응시할 퀴즈가 없습니다.");
        }
        return quizzes;
    }

    private void validateSubmittedAnswers(List<Quiz> quizzes, Map<Long, Long> selectedOptionIdsByQuizId) {
        if (selectedOptionIdsByQuizId == null) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "모든 문항에 답안을 선택해 주세요.");
        }

        Map<Long, Quiz> quizzesById = quizzes.stream()
                .collect(Collectors.toMap(Quiz::getQuizId, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        if (!selectedOptionIdsByQuizId.keySet().equals(quizzesById.keySet())) {
            throw new BaseException(ErrorResponseCode.BAD_REQUEST, "모든 문항에 답안을 선택해 주세요.");
        }

        for (Quiz quiz : quizzes) {
            Long selectedOptionId = selectedOptionIdsByQuizId.get(quiz.getQuizId());
            boolean belongsToQuiz = quizMapper.findOptionsByQuizId(quiz.getQuizId()).stream()
                    .anyMatch(option -> option.getOptionId().equals(selectedOptionId));
            if (!belongsToQuiz) {
                throw new BaseException(ErrorResponseCode.BAD_REQUEST, "선택한 답안이 퀴즈 보기와 일치하지 않습니다.");
            }
        }
    }

    private List<ChapterQuizQuestionResponse> findAttemptQuestions(Long chapterId) {
        List<ChapterQuizQuestionResponse> questions = quizMapper.findAttemptQuestionsByChapterId(chapterId);
        if (questions == null || questions.isEmpty()) {
            throw new BaseException(ErrorResponseCode.NOT_FOUND_ENDPOINT, "응시할 퀴즈가 없습니다.");
        }

        for (ChapterQuizQuestionResponse question : questions) {
            List<ChapterQuizOptionResponse> options = quizMapper.findAttemptOptionsByQuizId(question.getQuizId());
            question.setOptions(options);
        }
        return questions;
    }

    private QuizOption findCorrectOption(Long quizId) {
        return quizMapper.findOptionsByQuizId(quizId).stream()
                .filter(option -> CORRECT_YN.equals(option.getCorrectYn()))
                .findFirst()
                .orElse(null);
    }

    private ChapterQuizResultItem toResultItem(QuizAnswerResultRow row) {
        return ChapterQuizResultItem.builder()
                .quizId(row.getQuizId())
                .questionText(row.getQuestionText())
                .selectedOptionId(row.getSelectedOptionId())
                .selectedOptionText(row.getSelectedOptionText())
                .correctOptionId(row.getCorrectOptionId())
                .correctOptionText(row.getCorrectOptionText())
                .correct(CORRECT_YN.equals(row.getCorrectYn()))
                .explanation(row.getExplanation())
                .build();
    }

    private int defaultNumber(Integer value) {
        return value == null ? 0 : value;
    }
}
