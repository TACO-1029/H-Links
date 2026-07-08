package com.hlinks.domain.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCourseCompletionStatusRow {

    private String courseTitle;
    private String courseTypeLabel;
    private String learnerName;
    private String departmentName;
    private int progressRate;
    private String quizStatusLabel;
    private String completionStatusLabel;
    private String completionTone;
    private String completedAtStr;
}
