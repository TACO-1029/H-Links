package com.hlinks.domain.course.entity;

import com.hlinks.domain.course.type.CourseType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course {

    private Long courseId;
    private Long createdBy;
    private String categoryType;
    private CourseType courseType;
    private String courseTitle;
    private String description;
    private String instructorName;
    private String thumbnailUrl;

    public String getCourseTypeValue() {
        return courseType == null ? null : courseType.name();
    }
}
