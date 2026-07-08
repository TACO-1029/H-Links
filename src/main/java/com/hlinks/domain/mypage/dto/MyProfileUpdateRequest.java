package com.hlinks.domain.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyProfileUpdateRequest {

    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Size(max = 30, message = "연락처는 30자 이하로 입력해 주세요.")
    @Pattern(regexp = "^[0-9+\\-()\\s]+$", message = "연락처 형식이 올바르지 않습니다.")
    private String phone;

    private String newPassword;

    private String newPasswordConfirm;
}
