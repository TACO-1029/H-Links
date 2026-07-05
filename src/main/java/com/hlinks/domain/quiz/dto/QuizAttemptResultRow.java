package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuizAttemptResultRow extends QuizAnswerResultRow {

    private Long attemptId;
    private Long courseId;
    private Long chapterId;
    private String courseTitle;
    private String chapterTitle;
    private LocalDateTime submittedAt;
}
