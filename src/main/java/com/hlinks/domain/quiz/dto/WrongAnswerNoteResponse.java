package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WrongAnswerNoteResponse {

    private Long wrongNoteId;
    private Long courseId;
    private Long chapterId;
    private Long quizId;
    private String courseTitle;
    private String chapterTitle;
    private String questionText;
    private String selectedOptionText;
    private String correctOptionText;
    private String explanation;
    private LocalDateTime createdAt;
}
