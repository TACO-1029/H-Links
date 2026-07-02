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
public class CourseListResponseDto {

    private Long courseId;            // 강의 고유 ID
    private String categoryType;      // 강의 운영 성격 (CAREER_HIGH / CAREER_PATH)
    private String courseType;        // 강의 유형 (ONLINE / OFFLINE)
    private String courseTitle;       // 강의명
    private String instructorName;    // 강사명
    private String thumbnailUrl;      // 썸네일 이미지 URL

}