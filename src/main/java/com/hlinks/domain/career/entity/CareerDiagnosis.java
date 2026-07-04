package com.hlinks.domain.career.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CareerDiagnosis {
    private Long diagnosisId;
    private Long userId;
    private String llmSummary;
    private String levelTestBuildStatus; // PENDING, PROCESSING, COMPLETED, FAILED
    private LocalDateTime createdAt;
}

