package com.hlinks.domain.quiz.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiQuizGenerateResponse {

    private List<AiGeneratedQuiz> quizzes;
}
