package com.hlinks.domain.career.ai.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiGeneratedLevelTestQuestion {
    private String questionText;
    private String difficulty; // LOW, MEDIUM, HIGH
    private String explanation;
    private String answerText;
    private List<AiGeneratedLevelTestOption> options;
}
