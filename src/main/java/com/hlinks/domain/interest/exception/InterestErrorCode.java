package com.hlinks.domain.interest.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements BaseResponseCode {

    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "INTEREST_001", "선택 가능한 관심분야가 없습니다."),
    INTEREST_REQUIRED(HttpStatus.BAD_REQUEST, "INTEREST_002", "관심분야를 1개 이상 선택해주세요."),
    INTEREST_TOO_MANY_SELECTED(HttpStatus.BAD_REQUEST, "INTEREST_003", "관심분야는 최대 5개까지 선택할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
