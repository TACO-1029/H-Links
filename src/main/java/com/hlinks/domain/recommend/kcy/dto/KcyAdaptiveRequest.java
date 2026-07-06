package com.hlinks.domain.recommend.kcy.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KcyAdaptiveRequest {
    private List<Long> selectedOptionIds; // 선택한 일반 옵션 ID
    private List<String> angerScoreTypes; // 하이라이터로 찾은 성향 (Wide, Deep, Action, Outline, Prompter, Manual, Independent, Corporate)
    private List<String> tiebreakerBlocks; // 테트리스에서 배치한 블록 리스트 (AI_COPILOT, VANILLA_CODE 등)
    private Integer timeTaken; // 테트리스 소요 시간 (초)
    private Integer fillRate; // 테트리스 채움 비율 (%)
}
