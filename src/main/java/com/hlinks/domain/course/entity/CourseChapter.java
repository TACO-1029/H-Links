package com.hlinks.domain.course.entity;

import com.hlinks.domain.quiz.type.QuizBuildStatus;
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
    private String videoPath;
    private String originalFileName;
    private Long fileSize;
    private Integer durationSeconds;
    private String summaryText;
    private String transcriptText;
    private String aiGeneratedYn;
    private QuizBuildStatus quizBuildStatus;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
