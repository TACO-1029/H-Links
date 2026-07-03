package com.hlinks.domain.recommend.kcy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KcySubmitRequest {

    @NotEmpty(message = "모든 문항에 응답해주세요.")
    private List<Long> selectedOptionIds;
}
