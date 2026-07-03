package com.hlinks.domain.quiz.dto;

import com.hlinks.domain.quiz.type.QuizBuildStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuizListResponse {

    private Long quizId;
    private Long courseId;
    private Long chapterId;

    private String courseTitle;
    private String chapterTitle;
    private String questionType;
    private String questionText;
    private String difficulty;
    private String status;
    private String aiGeneratedYn;
    private String quizBuildStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime sortAt;

    public String getQuestionTypeLabel() {
        if (quizId == null) {
            return "생성 상태";
        }

        if ("MULTIPLE_CHOICE".equals(questionType)) {
            return "객관식";
        }

        return "퀴즈";
    }

    public String getDifficultyLabel() {
        if (quizId == null) {
            return "-";
        }

        if ("EASY".equals(difficulty)) {
            return "쉬움";
        }

        if ("MEDIUM".equals(difficulty)) {
            return "보통";
        }

        if ("HARD".equals(difficulty)) {
            return "어려움";
        }

        return "미지정";
    }

    public String getReviewStatusLabel() {
        if ("DRAFT".equals(status)) {
            return "검토 대기";
        }

        if ("APPROVED".equals(status)) {
            return "승인됨";
        }

        if ("REJECTED".equals(status)) {
            return "반려됨";
        }

        return "미지정";
    }

    public String getStatusLabel() {
        return getReviewStatusLabel();
    }

    public String getDisplayQuestionText() {
        if (questionText != null && !questionText.isBlank()) {
            return questionText;
        }

        if ("PENDING".equals(quizBuildStatus)) {
            return "AI 퀴즈 생성 대기 중입니다.";
        }

        if ("PROCESSING".equals(quizBuildStatus)) {
            return "AI 퀴즈를 생성하고 있습니다.";
        }

        if ("FAILED".equals(quizBuildStatus)) {
            return "AI 퀴즈 생성에 실패했습니다.";
        }

        return "AI 퀴즈 생성 상태를 확인하고 있습니다.";
    }

    public String getBuildStatusLabel() {
        if (quizBuildStatus == null || quizBuildStatus.isBlank()) {
            return "미지정";
        }

        try {
            return QuizBuildStatus.valueOf(quizBuildStatus).getDescription();
        } catch (IllegalArgumentException e) {
            return "미지정";
        }
    }

    public String getQuizBuildStatusLabel() {
        return getBuildStatusLabel();
    }
}
