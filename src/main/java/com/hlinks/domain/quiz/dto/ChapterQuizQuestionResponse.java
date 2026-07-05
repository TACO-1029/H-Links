package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChapterQuizQuestionResponse {

    private Long quizId;
    private String questionText;
    private List<ChapterQuizOptionResponse> options;
}
