package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseApplyTargetDto {

    private Long courseId;
    private String courseType;
    private String courseStatus;
    private LocalDateTime applyStartDate;
    private LocalDateTime applyEndDate;
    private Integer capacity;
    private Integer currentApplicantCount;
}
