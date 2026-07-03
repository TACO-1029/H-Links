package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressResponseDto {

    private Integer lastPlaySeconds;
    private Integer maxPlaySeconds;
    private Integer progressRate;
    private String status;
}
