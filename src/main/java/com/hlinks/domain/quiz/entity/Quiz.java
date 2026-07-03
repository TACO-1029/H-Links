package com.hlinks.domain.quiz.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Quiz {

    private Long quizId;
    private Long courseId;
    private Long chapterId;

    private String courseTitle;
    private String chapterTitle;
    private String questionType;
    private String questionText;
    private String answerText;
    private String explanation;
    private String difficulty;
    private String status;
    private String aiGeneratedYn;
    private String quizBuildStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sortAt;
}
