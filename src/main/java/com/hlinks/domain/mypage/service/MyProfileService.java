package com.hlinks.domain.mypage.service;

import com.hlinks.domain.mypage.dto.MyProfileUpdateRequest;
import com.hlinks.domain.mypage.dto.MyProfileUpdateResponse;

public interface MyProfileService {
    MyProfileUpdateResponse updateProfile(Long userId, MyProfileUpdateRequest request);
}
