package com.hlinks.global.response;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BaseResponse {

    private final boolean success;
    private final String code;
    private final String message;
    private final String timestamp;

    /*
    BaseResponse 자체를 직접 만드는 게 아니라 아래처럼 쓰게 하기 위해서 protected로 선언했습니다. 부모 역할만 하도록
    - SuccessResponse.from(data)
    - ErrorResponse.from(errorCode)
     */
    protected BaseResponse(boolean success, BaseResponseCode responseCode) {
        this.success = success;
        this.code = responseCode.getCode();
        this.message = responseCode.getMessage();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    protected BaseResponse(boolean success, BaseResponseCode responseCode, String message) {
        this.success = success;
        this.code = responseCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
