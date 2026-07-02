package com.hlinks.domain.quiz.mapper;

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

    List<QuizOption> findOptionsByQuizId(@Param("quizId") Long quizId);
}
