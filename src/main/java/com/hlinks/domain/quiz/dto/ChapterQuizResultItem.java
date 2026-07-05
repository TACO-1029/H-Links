package com.hlinks.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChapterQuizResultItem {

    private Long quizId;
    private String questionText;
    private Long selectedOptionId;
    private String selectedOptionText;
    private Long correctOptionId;
    private String correctOptionText;
    private boolean correct;
    private String explanation;
}
