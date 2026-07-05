package com.hlinks.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChapterQuizQuestionResponse {

    private Long quizId;
    private String questionText;
    private String explanation;
    private List<QuizOptionResponse> options;
}
