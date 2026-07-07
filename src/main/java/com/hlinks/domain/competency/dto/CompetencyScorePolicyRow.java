package com.hlinks.domain.competency.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompetencyScorePolicyRow {

    private Long competencyId;
    private String calcType;
    private BigDecimal scoreDelta;
}
