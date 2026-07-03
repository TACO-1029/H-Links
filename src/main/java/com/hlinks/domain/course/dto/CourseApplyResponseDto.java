package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseApplyResponseDto {

    private Long applicationId;
    private Long courseLearningId;
    private Long courseId;
    private String applicationStatus;
    private String learningStatus;
}
