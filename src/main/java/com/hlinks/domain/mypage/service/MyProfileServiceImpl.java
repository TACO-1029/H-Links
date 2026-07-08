package com.hlinks.domain.mypage.service;

import com.hlinks.domain.mypage.dto.MyProfileUpdateRequest;
import com.hlinks.domain.mypage.dto.MyProfileUpdateResponse;
import com.hlinks.domain.mypage.exception.MyPageErrorCode;
import com.hlinks.domain.user.dto.LoginUserDto;
import com.hlinks.domain.user.mapper.UserMapper;
import com.hlinks.global.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MyProfileServiceImpl implements MyProfileService {

    private static final DateTimeFormatter PROFILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MyProfileUpdateResponse updateProfile(Long userId, MyProfileUpdateRequest request) {
        LoginUserDto user = userMapper.findByUserId(userId)
                .orElseThrow(() -> new BaseException(MyPageErrorCode.PROFILE_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BaseException(MyPageErrorCode.CURRENT_PASSWORD_MISMATCH);
        }

        String encodedNewPassword = null;
        if (StringUtils.hasText(request.getNewPassword()) || StringUtils.hasText(request.getNewPasswordConfirm())) {
            if (!StringUtils.hasText(request.getNewPassword()) || request.getNewPassword().length() < 8) {
                throw new BaseException(MyPageErrorCode.NEW_PASSWORD_TOO_SHORT);
            }

            if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
                throw new BaseException(MyPageErrorCode.NEW_PASSWORD_CONFIRM_MISMATCH);
            }

            encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        }

        int updatedCount = userMapper.updateMyProfile(userId, request.getPhone(), encodedNewPassword);
        if (updatedCount == 0) {
            throw new BaseException(MyPageErrorCode.PROFILE_NOT_FOUND);
        }

        return MyProfileUpdateResponse.builder()
                .phone(request.getPhone())
                .updatedAt(LocalDateTime.now().format(PROFILE_DATE_FORMATTER))
                .build();
    }
}
