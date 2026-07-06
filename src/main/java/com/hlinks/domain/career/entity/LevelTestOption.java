package com.hlinks.domain.career.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelTestOption {
    private Long levelOptionId;
    private Long levelQuestionId;
    private String optionNo;
    private String optionText;
    private String correctYn;
}
