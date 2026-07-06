package com.hlinks.domain.career.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CareerErrorCode implements BaseResponseCode {

    SKILL_REQUIRED(HttpStatus.BAD_REQUEST, "CAREER_001", "목표 스킬을 1개 이상 선택해주세요."),
    DIAGNOSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "CAREER_002", "진단 이력을 찾을 수 없습니다."),
    INVALID_SKILL_COUNT(HttpStatus.BAD_REQUEST, "CAREER_003", "진단 가능한 목표 스킬 개수는 1개에서 최대 3개까지입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
