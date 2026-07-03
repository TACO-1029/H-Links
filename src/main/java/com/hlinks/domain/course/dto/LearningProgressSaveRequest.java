package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LearningProgressSaveRequest {

    private Integer lastPlaySeconds;
    private Integer durationSeconds;
    private String eventType;
    private Boolean flush;
    private Boolean completed;
}
