package com.hlinks.domain.career.ai.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGeneratedLevelTestOption {
    private Integer optionNo;
    private String optionText;
    private String correctYn;
    private String explanation;
}
