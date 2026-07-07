package com.hlinks.domain.recommend.kcy.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KcyMatchCandidateDto {

    private Long userId;
    private String name;
    private String departmentName;
    private String jobName;
    private String positionName;
    private String kcyResult;
}
