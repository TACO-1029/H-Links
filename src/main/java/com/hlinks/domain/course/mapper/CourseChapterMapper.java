package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.entity.CourseChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseChapterMapper {

    CourseChapter findById(@Param("chapterId") Long chapterId);

    List<CourseChapter> findPendingQuizBuildChapters(@Param("limit") int limit);

    int updateTranscriptText(
            @Param("chapterId") Long chapterId,
            @Param("transcriptText") String transcriptText
    );
}