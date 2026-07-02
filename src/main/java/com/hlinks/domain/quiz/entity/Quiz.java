package com.hlinks.domain.quiz.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Quiz {

    private Long quizId;
    private Long courseId;
    private Long chapterId;

    private String questionType;
    private String questionText;
    private String answerText;
    private String explanation;
    private String difficulty;
    private String status;
    private String aiGeneratedYn;
}