package com.hlinks.global.config;

import com.hlinks.global.security.CustomLoginSuccessHandler;
import com.hlinks.global.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CSRF는 끄지 않습니다. Thymeleaf form/fetch에서 token을 전달합니다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/fonts/**"
                        ).permitAll()

                        // Course Chapter API
                        // 조회는 로그인한 일반 사용자도 가능
                        .requestMatchers(HttpMethod.GET, "/api/course-chapters/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_HR", "ROLE_LEADER")

                        // 생성/수정/삭제는 HR만 가능
                        .requestMatchers(HttpMethod.POST, "/api/course-chapters/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.PUT, "/api/course-chapters/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.PATCH, "/api/course-chapters/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/course-chapters/**").hasAuthority("ROLE_HR")

                        // Quiz API
                        // 퀴즈 조회는 일반 사용자도 가능
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**")
                        .hasAnyAuthority("ROLE_USER", "ROLE_HR", "ROLE_LEADER")

                        // AI 퀴즈 생성/저장 등 상태 변경은 HR만 가능
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.PATCH, "/api/quizzes/**").hasAuthority("ROLE_HR")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").hasAuthority("ROLE_HR")

                        // Page routes
                        .requestMatchers("/team/**").hasAuthority("ROLE_LEADER")
                        .requestMatchers("/hr/**").hasAuthority("ROLE_HR")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}