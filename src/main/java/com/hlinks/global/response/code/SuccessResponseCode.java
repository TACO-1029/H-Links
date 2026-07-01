package com.hlinks.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessResponseCode implements BaseResponseCode {

    SUCCESS_OK(HttpStatus.OK, "SUCCESS_200", "요청이 성공했습니다."),
    SUCCESS_CREATED(HttpStatus.CREATED, "SUCCESS_201", "리소스가 생성되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
