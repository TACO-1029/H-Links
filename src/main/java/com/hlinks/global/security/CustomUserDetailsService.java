package com.hlinks.global.security;

import com.hlinks.domain.user.dto.LoginUserDto;
import com.hlinks.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 로그인 과정에서 "이 loginId의 사용자를 찾아줘" 라고 호출하는 진입점입니다.
 * 여기서 MyBatis UserMapper를 사용
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        LoginUserDto user = userMapper.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        List<String> roleList = userMapper.findRoleCodesByUserId(user.getUserId());

        return new CustomUserDetails(user, roleList);
    }
}
