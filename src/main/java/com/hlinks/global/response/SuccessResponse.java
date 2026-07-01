package com.hlinks.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.hlinks.global.response.code.BaseResponseCode;
import com.hlinks.global.response.code.SuccessResponseCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
@JsonPropertyOrder({"success", "timestamp", "code", "httpStatus", "message", "data"})
public class SuccessResponse<T> extends BaseResponse {

    private final int httpStatus;
    private final T data;

    private SuccessResponse(T data, BaseResponseCode responseCode) {
        super(true, responseCode);
        this.httpStatus = responseCode.getHttpStatus().value();
        this.data = data;
    }

    /*
    가장 많이 쓸 기본 성공 응답
    - SuccessResponse.from(user)
     */
    public static <T> SuccessResponse<T> from(T data) {
        return new SuccessResponse<>(data, SuccessResponseCode.SUCCESS_OK);
    }
    // 성공했지만 내려줄 데이터가 없을 때 사용
    public static SuccessResponse<Void> empty() {
        return new SuccessResponse<>(null, SuccessResponseCode.SUCCESS_OK);
    }
    // 성공 코드까지 직접 지정하고 싶을 때 사용
    public static <T> SuccessResponse<T> of(T data, BaseResponseCode responseCode) {
        return new SuccessResponse<>(data, responseCode);
    }
    // 데이터 없이 성공 코드만 지정하고 싶을 때 사용
    public static SuccessResponse<Void> from(BaseResponseCode responseCode) {
        return new SuccessResponse<>(null, responseCode);
    }
}
