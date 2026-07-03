package com.hlinks.domain.quiz.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuizBuildStatus {

    PENDING("생성 대기"),
    PROCESSING("퀴즈 생성 중"),
    COMPLETED("생성 완료"),
    FAILED("생성 실패");
    private final String description;
}
