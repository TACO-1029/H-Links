package com.hlinks.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChapterQuizResultResponse {

    private Long attemptId;
    private Long courseId;
    private Long chapterId;
    private String courseTitle;
    private String chapterTitle;
    private int totalCount;
    private int correctCount;
    private LocalDateTime submittedAt;
    private List<ChapterQuizResultItem> items;
}
