package com.hlinks.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonPropertyOrder({"success", "timestamp", "code", "httpStatus", "message", "data"})
public class ErrorResponse<T> extends BaseResponse {

    private final int httpStatus;
    private final T data;

    private ErrorResponse(BaseResponseCode responseCode, T data) {
        super(false, responseCode);
        this.httpStatus = responseCode.getHttpStatus().value();
        this.data = data;
    }

    private ErrorResponse(BaseResponseCode responseCode, T data, String message) {
        super(false, responseCode, message);
        this.httpStatus = responseCode.getHttpStatus().value();
        this.data = data;
    }

    public static ErrorResponse<Void> from(BaseResponseCode responseCode) {
        return new ErrorResponse<>(responseCode, null);
    }
    /*
    실패는 했으나, 메세지 커스텀 시 사용하도록 열어두었습니다
    - 사번은 필수입니다.
    - 비밀번호는 8자 이상이어야 합니다.
    - 입사일은 오늘 이전이어야 합니다.
     */
    public static ErrorResponse<Void> from(BaseResponseCode responseCode, String message) {
        return new ErrorResponse<>(responseCode, null, message);
    }

    public static <T> ErrorResponse<T> of(BaseResponseCode responseCode, T data) {
        return new ErrorResponse<>(responseCode, data);
    }

    public static <T> ErrorResponse<T> of(BaseResponseCode responseCode, T data, String message) {
        return new ErrorResponse<>(responseCode, data, message);
    }
}
