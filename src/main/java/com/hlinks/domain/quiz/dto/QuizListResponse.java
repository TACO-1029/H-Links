package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuizListResponse {

    private Long quizId;
    private Long courseId;
    private Long chapterId;

    private String questionType;
    private String questionText;
    private String difficulty;
    private String status;
    private String aiGeneratedYn;

    private LocalDateTime createdAt;
}
