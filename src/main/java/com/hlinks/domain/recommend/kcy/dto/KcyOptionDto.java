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
}
