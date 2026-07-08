package com.hlinks.global.security;

import com.hlinks.domain.user.dto.LoginUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security는 우리가 만든 LoginUserDto를 바로 인증 객체로 쓰지 않고, UserDetails 타입을 요구합니다. 이에따라 LoginUserDto +
 * roleCodes를 Spring Security가 이해하는 형태로 포장
 */
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final LoginUserDto user;
    private final List<String> roleList;

    // getUserId, getName 다 로그인 이후에 컨트롤러나 서비스에서 현재 로그인 사용자의 정보를 꺼낼 때 쓰는 편의 메서드
    public Long getUserId() {
        return user.getUserId();
    }

    public String getName() {
        return user.getName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getPhone() {
        return user.getPhone();
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return user.getUpdatedAt();
    }

    public void updateProfile(String phone, java.time.LocalDateTime updatedAt) {
        user.setPhone(phone);
        user.setUpdatedAt(updatedAt);
    }

    public Long getDepartmentId() {
        return user.getDepartmentId();
    }

    public String getDepartmentName() {
        return user.getDepartmentName();
    }

    public Long getJobId() {
        return user.getJobId();
    }

    public String getJobName() {
        return user.getJobName();
    }

    public Long getPositionId() {
        return user.getPositionId();
    }

    public String getPositionName() {
        return user.getPositionName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(user.getStatus());
    }
}
