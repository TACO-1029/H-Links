package com.hlinks.domain.interest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterestDto {

    // 사용자의 관심분야는 사실 SKILL 테이블의 값으로 만들어지기 때문에 skillId로 명명했습니다.
    private Long skillId;
    private String skillName;
    private String skillType;
}

