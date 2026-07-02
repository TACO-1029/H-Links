package com.hlinks.domain.quiz.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedQuiz {

    private String questionType;
    private String questionText;
    private String difficulty;
    private String explanation;
    private String answerText;
    private List<AiGeneratedQuizOption> options;
}
