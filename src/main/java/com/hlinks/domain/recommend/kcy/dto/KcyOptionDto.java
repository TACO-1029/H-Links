package com.hlinks.domain.recommend.kcy.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KcyOptionDto {

    private Long kcyOptionId;
    private Long kcyQuestionId;
    private String optionNo;
    private String optionText;

    // 성향 가중치 점수 컬럼
    private int actionScore;
    private int outlineScore;
    private int wideScore;
    private int deepScore;
    private int independentScore;
    private int corporateScore;
    private int prompterScore;
    private int manualScore;
}
