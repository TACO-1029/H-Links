package com.hlinks.domain.mypage.exception;

import com.hlinks.global.response.code.BaseResponseCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MyPageErrorCode implements BaseResponseCode {

    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "MYPAGE_404_1", "사용자 정보를 찾을 수 없습니다."),
    CURRENT_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "MYPAGE_400_1", "현재 비밀번호가 올바르지 않습니다."),
    NEW_PASSWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "MYPAGE_400_2", "새 비밀번호는 8자 이상이어야 합니다."),
    NEW_PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "MYPAGE_400_3", "새 비밀번호 확인이 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
