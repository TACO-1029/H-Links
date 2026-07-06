package com.hlinks.domain.career.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LevelTestAnswerLog {
    private Long levelAnswerId;
    private Long userId;
    private Long levelQuestionId;
    private Long selectedOptionId;
    private String correctYn;
}
