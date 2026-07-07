package com.hlinks.domain.recommend.kcy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KcyOptionClientDto {
    private Long kcyOptionId;
    private Long kcyQuestionId;
    private String optionNo;
    private String optionText;

    public static KcyOptionClientDto from(KcyOptionDto dto) {
        if (dto == null) return null;
        return new KcyOptionClientDto(
            dto.getKcyOptionId(),
            dto.getKcyQuestionId(),
            dto.getOptionNo(),
            dto.getOptionText()
        );
    }
}
