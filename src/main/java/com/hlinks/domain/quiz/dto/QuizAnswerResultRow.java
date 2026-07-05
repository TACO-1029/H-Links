package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerResultRow {

    private Long quizId;
    private String questionText;
    private String explanation;
    private Long selectedOptionId;
    private String selectedOptionText;
    private Long correctOptionId;
    private String correctOptionText;
    private String correctYn;
}
