package com.hlinks.domain.course.dto;

import com.hlinks.domain.course.type.CourseType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AdminCourseCreateRequest {

    private String courseTitle;
    private String description;
    private String categoryType;
    private CourseType courseType;
    private String instructorName;
    private MultipartFile thumbnailFile;
    private String courseMaterialUrl;
    private Integer capacity;
    private String location;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private LocalDate courseStartDate;
    private LocalDate courseEndDate;
    private List<String> chapterTitles;
    private List<MultipartFile> videoFiles;
}
