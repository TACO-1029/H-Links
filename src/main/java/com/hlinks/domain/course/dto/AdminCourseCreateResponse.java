package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminCourseCreateResponse {

    private Long courseId;
    private List<Long> chapterIds;
}
