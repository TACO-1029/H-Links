package com.hlinks.domain.course.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseChapter {

    private Long chapterId;
    private Long courseId;
    private String chapterTitle;
    private Integer chapterOrder;
    private String videoUrl;
    private Integer durationSeconds;
    private String summaryText;
    private String transcriptText;
    private String aiGeneratedYn;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}