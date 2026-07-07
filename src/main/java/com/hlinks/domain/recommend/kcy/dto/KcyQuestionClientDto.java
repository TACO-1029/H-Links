package com.hlinks.domain.recommend.kcy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KcyQuestionClientDto {
    private Long kcyQuestionId;
    private String questionText;
    private Integer sortOrder;
    private List<KcyOptionClientDto> options;

    public static KcyQuestionClientDto from(KcyQuestionDto dto) {
        if (dto == null) return null;
        List<KcyOptionClientDto> clientOptions = dto.getOptions() == null ? null :
            dto.getOptions().stream().map(KcyOptionClientDto::from).collect(Collectors.toList());
        return new KcyQuestionClientDto(
            dto.getKcyQuestionId(),
            dto.getQuestionText(),
            dto.getSortOrder(),
            clientOptions
        );
    }
}
