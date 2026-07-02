package com.hlinks.domain.quiz.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiGeneratedQuizOption {

    private Integer optionNo;
    private String optionText;
    private String correctYn;
}
