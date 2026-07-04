package com.hlinks.domain.course.dto;

import com.hlinks.domain.quiz.type.QuizBuildStatus;
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
    private Integer progressRate;
    private Integer lastPlaySeconds;
    private Integer maxPlaySeconds;
    private String learningStatus;

    public String getPlayableVideoUrl() {
        String source = hasText(videoUrl) ? videoUrl : videoPath;
        if (!hasText(source)) {
            return null;
        }

        String normalized = source.replace("\\", "/");
        if (normalized.startsWith("./storage/uploads/")) {
            return normalized.replaceFirst("^\\./storage/uploads", "/uploads");
        }
        if (normalized.startsWith("storage/uploads/")) {
            return "/" + normalized.substring(normalized.indexOf("uploads/"));
        }

        String marker = "/storage/uploads/";
        int markerIndex = normalized.indexOf(marker);
        if (markerIndex >= 0) {
            return "/uploads/" + normalized.substring(markerIndex + marker.length());
        }

        return source;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String aiGeneratedYn;
    private QuizBuildStatus quizBuildStatus;

    public String getQuizBuildStatusLabel() {
        return quizBuildStatus == null ? "생성 대기" : quizBuildStatus.getDescription();
    }
}
