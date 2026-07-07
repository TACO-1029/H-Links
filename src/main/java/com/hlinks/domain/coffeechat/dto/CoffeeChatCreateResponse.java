package com.hlinks.domain.coffeechat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CoffeeChatCreateResponse {

    private Long requestId;
    private String status;
    private String mailStatus;
}
