package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/* 퀴즈 상세 조회용 */
@Getter
@Setter
public class QuizResponse {
    private Long quizId;
    private Long courseId;
    private Long chapterId;

    private String questionType;
    private String questionText;
    private String explanation;
    private String difficulty;
    private String answerText;

    private String status;
    private String aiGeneratedYn;

    private LocalDateTime createdAt;

    private List<QuizOptionResponse> options;
}
