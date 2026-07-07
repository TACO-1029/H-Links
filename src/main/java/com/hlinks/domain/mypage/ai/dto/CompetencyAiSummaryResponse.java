package com.hlinks.domain.mypage.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompetencyAiSummaryResponse {

    private String headline;
    private String strength;
    private String improvement;
    private String growthPotential;
    private String nextAction;
}
