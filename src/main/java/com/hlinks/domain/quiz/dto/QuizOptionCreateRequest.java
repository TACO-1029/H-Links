package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

/* 객관식 보기 저장용*/
@Getter
@Setter
public class QuizOptionCreateRequest {
    private String optionNo;
    private String optionText;
    private String correctYn; // Y 또는 N
}
