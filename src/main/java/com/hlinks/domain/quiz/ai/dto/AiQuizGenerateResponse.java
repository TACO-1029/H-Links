package com.hlinks.domain.quiz.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuizGenerateResponse {

    private List<AiGeneratedQuizDto> quizzes;
}