package com.hlinks.domain.recommend.kcy.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KcySubmitRequest {

    private List<Long> selectedOptionIds;
    private List<String> angerScoreTypes;
    private List<String> tiebreakerBlocks;
    private Integer timeTaken;
    private Integer fillRate;
}
