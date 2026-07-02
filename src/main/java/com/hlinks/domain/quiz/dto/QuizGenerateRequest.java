package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

/* AI로 부터 퀴즈 생성*/
@Getter
@Setter
public class QuizGenerateRequest {

    private Long courseId;
    private Long chapterId;

    // mp3 stt sourceText
    private String sourceText;

    private Integer quizCount;
    private String difficulty;
}
