package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.entity.CourseChapter;
import com.hlinks.domain.quiz.type.QuizBuildStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseChapterMapper {

    CourseChapter findById(@Param("chapterId") Long chapterId);

    List<CourseChapter> findPendingQuizBuildChapters(@Param("limit") int limit);

    int updateQuizBuildStatus(
            @Param("chapterId") Long chapterId,
            @Param("quizBuildStatus") QuizBuildStatus quizBuildStatus
    );

    int updateQuizBuildStatusIfPending(
            @Param("chapterId") Long chapterId,
            @Param("quizBuildStatus") QuizBuildStatus quizBuildStatus
    );

    int updateTranscriptText(
            @Param("chapterId") Long chapterId,
            @Param("transcriptText") String transcriptText
    );

    int updateSummaryText(
            @Param("chapterId") Long chapterId,
            @Param("summaryText") String summaryText
    );
}
