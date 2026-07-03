package com.hlinks.global.security;

import com.hlinks.domain.interest.service.InterestService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final InterestService interestService;

    /*
    커스텀 예외처리가 불가능한 이유
    - Spring Security 인터페이스에서 이미 이렇게 정의돼 있음 -> 부모 인터페이스 계약을 구현하는 것.
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        if (!interestService.hasInterests(customUserDetails.getUserId())) {
            res.sendRedirect("/interests/setup");
            return;
        }

        res.sendRedirect("/");
    }
}
