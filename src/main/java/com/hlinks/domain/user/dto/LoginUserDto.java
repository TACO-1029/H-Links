package com.hlinks.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoginUserDto {
    private Long userId;

    private Long departmentId;
    private String departmentName;

    private Long jobId;
    private String jobName;

    private Long positionId;
    private String positionName;

    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime updatedAt;
}
