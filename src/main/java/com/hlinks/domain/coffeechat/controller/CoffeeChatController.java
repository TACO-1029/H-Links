package com.hlinks.domain.coffeechat.controller;

import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateRequest;
import com.hlinks.domain.coffeechat.dto.CoffeeChatCreateResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingResponse;
import com.hlinks.domain.coffeechat.dto.CoffeeChatSettingUpdateRequest;
import com.hlinks.domain.coffeechat.service.CoffeeChatService;
import com.hlinks.global.response.SuccessResponse;
import com.hlinks.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coffee-chats")
public class CoffeeChatController {

    private final CoffeeChatService coffeeChatService;

    @PatchMapping("/settings")
    public SuccessResponse<CoffeeChatSettingResponse> updateSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoffeeChatSettingUpdateRequest request
    ) {
        return SuccessResponse.from(coffeeChatService.updateSetting(userDetails.getUserId(), request));
    }

    @PostMapping("/requests")
    public SuccessResponse<CoffeeChatCreateResponse> requestCoffeeChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoffeeChatCreateRequest request
    ) {
        return SuccessResponse.from(coffeeChatService.requestCoffeeChat(userDetails.getUserId(), request));
    }

    @PatchMapping("/requests/{requestId}/accept")
    public SuccessResponse<Void> acceptRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("requestId") Long requestId
    ) {
        coffeeChatService.acceptRequest(userDetails.getUserId(), requestId);
        return SuccessResponse.empty();
    }

    @DeleteMapping("/requests/{requestId}")
    public SuccessResponse<Void> rejectRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("requestId") Long requestId
    ) {
        coffeeChatService.rejectRequest(userDetails.getUserId(), requestId);
        return SuccessResponse.empty();
    }
}
