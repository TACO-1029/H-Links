package com.hlinks.domain.quiz.mapper;

import com.hlinks.domain.quiz.dto.QuizAnswerResultRow;
import com.hlinks.domain.quiz.dto.QuizAttemptResultRow;
import com.hlinks.domain.quiz.dto.QuizAttemptTargetDto;
import com.hlinks.domain.quiz.dto.WrongAnswerNoteResponse;
import com.hlinks.domain.quiz.entity.Quiz;
import com.hlinks.domain.quiz.entity.QuizOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuizMapper {

    int insertQuiz(Quiz quiz);

    int insertQuizOption(QuizOption option);

    Quiz findById(@Param("quizId") Long quizId);

    List<Quiz> findByCourseId(@Param("courseId") Long courseId);

    List<Quiz> findByChapterId(@Param("chapterId") Long chapterId);

    List<Quiz> findAiGeneratedQuizzes();

    List<QuizOption> findOptionsByQuizId(@Param("quizId") Long quizId);

    QuizAttemptTargetDto findQuizAttemptTarget(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId
    );

    Long findAttemptIdByUserAndChapter(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId
    );

    Long findAttemptOwnerUserId(@Param("attemptId") Long attemptId);

    Long nextQuizAttemptId();

    void insertQuizAttempt(
            @Param("attemptId") Long attemptId,
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId
    );

    void insertQuizAttemptAnswer(
            @Param("attemptId") Long attemptId,
            @Param("quizId") Long quizId,
            @Param("selectedOptionId") Long selectedOptionId,
            @Param("correctYn") String correctYn
    );

    List<QuizAnswerResultRow> findAttemptAnswerResults(@Param("attemptId") Long attemptId);

    List<QuizAttemptResultRow> findAttemptResult(@Param("attemptId") Long attemptId);

    List<WrongAnswerNoteResponse> findWrongAnswerNotes(
            @Param("userId") Long userId,
            @Param("courseId") Long courseId,
            @Param("chapterId") Long chapterId
    );
}
