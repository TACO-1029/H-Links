package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.entity.CourseChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseChapterMapper {

    CourseChapter findById(@Param("chapterId") Long chapterId);

    int updateTranscriptText(
            @Param("chapterId") Long chapterId,
            @Param("transcriptText") String transcriptText
    );
}