package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class AdminCourseCreateRequest {

    private String courseTitle;
    private String description;
    private String categoryType;
    private String instructorName;
    private List<String> chapterTitles;
    private List<MultipartFile> videoFiles;
}
