package com.hlinks.domain.coffeechat.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoffeeChatSettingUpdateRequest {

    @NotNull(message = "커피챗 알림 수신 여부를 선택해주세요.")
    private Boolean emailNotifyEnabled;
}
