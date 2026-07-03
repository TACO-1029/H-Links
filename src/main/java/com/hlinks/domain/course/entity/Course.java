package com.hlinks.domain.course.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course {

    private Long courseId;
    private Long createdBy;
    private String categoryType;
    private String courseType;
    private String courseTitle;
    private String description;
    private String instructorName;
}
