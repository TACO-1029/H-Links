package com.hlinks.domain.coffeechat.service;

import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateRequest;
import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatHistoryDto;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingUpdateRequest;

import java.util.List;

public interface CoffeeChatService {

    CoffeeChatSettingResponse getSetting(Long userId);

    CoffeeChatSettingResponse updateSetting(Long userId, CoffeeChatSettingUpdateRequest request);

    CoffeeChatCreateResponse requestCoffeeChat(Long requesterUserId, CoffeeChatCreateRequest request);

    CoffeeChatCreateResponse sendCoffeeChatMail(Long requesterUserId, CoffeeChatCreateRequest request);

    List<CoffeeChatHistoryDto> getSentRequests(Long userId);

    List<CoffeeChatHistoryDto> getReceivedRequests(Long userId);
}
