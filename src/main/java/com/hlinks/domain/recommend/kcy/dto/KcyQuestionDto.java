package com.hlinks.domain.recommend.kcy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KcyQuestionDto {

    private Long kcyQuestionId;
    private String questionText;
    private Integer sortOrder;
    private List<KcyOptionDto> options = new ArrayList<>();

    public void addOption(KcyOptionDto option) {
        this.options.add(option);
    }
}
