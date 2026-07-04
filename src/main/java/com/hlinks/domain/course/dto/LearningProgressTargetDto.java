package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LearningProgressTargetDto {

    private Long courseLearningId;
    private Long chapterLearningId;
    private Long courseId;
    private Long chapterId;
    private Integer durationSeconds;
    private Integer lastPlaySeconds;
    private Integer maxPlaySeconds;
    private Integer progressRate;
    private String status;
}
