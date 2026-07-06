package com.hlinks.domain.recommend.kcy.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KcyAdaptiveResponse {
    private String status; // IN_PROGRESS_MCQ, TETRIS_PHASE, TIEBREAKER, COMPLETED
    private KcyQuestionDto nextQuestion;
    private int currentCount;
    private int totalPredictCount;
    private java.util.List<TetrisBlockDto> blocks;
}
