package com.hlinks.domain.course.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnlineChapterAccessTargetDto {

    private Long courseId;
    private String courseType;
    private String onlineStatus;
    private String applicationStatus;
    private Long courseLearningId;
    private Long chapterId;
    private Long chapterLearningStatusId;
}
