package com.hlinks.domain.career.entity;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class LevelTestQuestion {
    private Long levelQuestionId;
    private Long diagnosisId;
    private Long skillId;
    private String questionText;
    private String questionType;
    private String difficulty;
    private String explanation;
    
    // For convenience
    private List<LevelTestOption> options;
}
