package com.hlinks.domain.mypage.dto;

import lombok.Builder;

@Builder
public record MyProfileUpdateResponse(
        String phone,
        String updatedAt
) {
}
