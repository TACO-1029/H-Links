package com.hlinks.domain.quiz.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizOption {

    private Long optionId;
    private Long quizId;
    private String optionNo;
    private String optionText;
    private String correctYn;
}
