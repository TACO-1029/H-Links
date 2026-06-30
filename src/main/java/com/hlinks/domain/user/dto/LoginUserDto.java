package com.hlinks.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUserDto {
    private Long userId;
    private String loginId;
    private String password;
    private String name;
    private String status;
}
