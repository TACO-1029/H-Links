package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizOptionResponse {

    private Long optionId;
    private Long quizId;

    private String optionNo;
    private String optionText;

    // 관리자 조회 시에만 내려주도록
    private String correctYn;
}
