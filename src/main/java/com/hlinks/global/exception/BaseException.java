package com.hlinks.global.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;

/*
RuntimeException은 unchecked exception라서 서비스 코드에서 메서드 선언에 throws를 강제하지 않습니다.
비즈니스 예외는 보통 RuntimeException으로 둡니다.
 */
@Getter
public class BaseException extends RuntimeException {

    private final BaseResponseCode responseCode;

    public BaseException(BaseResponseCode responseCode) {
        // 예외 자체의 메시지가 비어 있을 수 있어서 넣어두었습니다.
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public BaseException(BaseResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public BaseException(BaseResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
    }
}
