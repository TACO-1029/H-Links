package com.hlinks.domain.recommend.kcy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KcySubmitRequest {

    @Size(max = 15, message = "선택된 옵션 개수가 너무 많습니다.")
    private List<Long> selectedOptionIds;

    @Size(max = 20, message = "코드 리뷰 유형 개수가 범위를 초과했습니다.")
    private List<String> angerScoreTypes;

    @Size(max = 10, message = "배치된 블록 개수가 범위를 초과했습니다.")
    private List<String> tiebreakerBlocks;

    @Min(value = 0, message = "소요 시간은 음수일 수 없습니다.")
    private Integer timeTaken;

    @Min(value = 0, message = "그리드 채움 비율은 0% 이상이어야 합니다.")
    @Max(value = 100, message = "그리드 채움 비율은 100%를 초과할 수 없습니다.")
    private Integer fillRate;
}
