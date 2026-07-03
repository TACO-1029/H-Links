package com.hlinks.domain.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterResponseDto {
    private Long chapterId;
    private Long courseId;
    private String chapterTitle;
    private Integer chapterOrder;
    private String videoUrl;
    private String videoPath;
    private String originalFileName;
    private Long fileSize;
    private Integer durationSeconds;
    private String summaryText;
    private String transcriptText;
}
