package com.hlinks.domain.coffeechat.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CoffeeChatErrorCode implements BaseResponseCode {

    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "COFFEE_CHAT_404_1", "커피챗 대상 사용자를 찾을 수 없습니다."),
    SELF_REQUEST_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "COFFEE_CHAT_400_1", "본인에게는 커피챗을 신청할 수 없습니다."),
    RECEIVER_NOTIFICATION_DISABLED(HttpStatus.BAD_REQUEST, "COFFEE_CHAT_400_2", "상대방이 커피챗 알림 수신을 꺼두었습니다."),
    REQUEST_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "COFFEE_CHAT_400_3", "이미 커피챗을 신청한 사용자입니다."),
    KCY_RESULT_REQUIRED(HttpStatus.BAD_REQUEST, "COFFEE_CHAT_400_4", "커피챗 신청에는 양쪽 사용자의 KCY 결과가 필요합니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "COFFEE_CHAT_404_2", "처리할 커피챗 요청을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
