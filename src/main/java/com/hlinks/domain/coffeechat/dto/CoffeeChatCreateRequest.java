package com.hlinks.domain.coffeechat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoffeeChatCreateRequest {

    @NotNull(message = "커피챗 대상 사용자가 필요합니다.")
    private Long receiverUserId;

    @Size(max = 1000, message = "커피챗 메시지는 1000자 이하로 입력해주세요.")
    private String message;
}
