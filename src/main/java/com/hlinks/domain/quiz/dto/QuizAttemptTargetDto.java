package com.hlinks.domain.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAttemptTargetDto {

    private Long courseLearningId;
    private Long chapterLearningId;
    private Long courseId;
    private Long chapterId;
    private String courseTitle;
    private String chapterTitle;
    private Integer progressRate;
    private String learningStatus;
    private Long quizAttemptId;
}
