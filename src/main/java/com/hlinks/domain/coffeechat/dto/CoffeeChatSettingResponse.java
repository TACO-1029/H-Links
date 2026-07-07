package com.hlinks.domain.coffeechat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoffeeChatSettingResponse {

    private boolean emailNotifyEnabled;
}
