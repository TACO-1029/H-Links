package com.hlinks.domain.coffeechat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoffeeChatUserProfileDto {

    private Long userId;
    private String name;
    private String email;
    private String departmentName;
    private String jobName;
    private String positionName;
    private String kcyResult;
    private String emailNotifyYn;
}
