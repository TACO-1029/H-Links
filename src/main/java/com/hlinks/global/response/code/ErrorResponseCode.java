package com.hlinks.global.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorResponseCode implements BaseResponseCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GLOBAL_400_1", "잘못된 요청입니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "GLOBAL_400_2", "HTTP 요청 바디의 형식이 잘못되었습니다."),
    INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "GLOBAL_400_3", "HTTP 요청 파라미터의 형식이 잘못되었습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GLOBAL_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "GLOBAL_403", "해당 요청에 접근 권한이 없습니다."),
    NOT_FOUND_ENDPOINT(HttpStatus.NOT_FOUND, "GLOBAL_404", "존재하지 않는 엔드포인트입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "GLOBAL_405", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL_500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
