package com.hlinks.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChapterQuizPageResponse {

    private Long courseId;
    private Long chapterId;
    private String courseTitle;
    private String chapterTitle;
    private Integer progressRate;
    private List<ChapterQuizQuestionResponse> questions;
}
