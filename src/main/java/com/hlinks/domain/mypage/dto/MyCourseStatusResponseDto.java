package com.hlinks.domain.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCourseStatusResponseDto {
    private int inProgressCount;
    private int completedCount;
    private int reinforcementCount; // 보완 필요 수

    private List<MyCourseDto> inProgressCourses;
    private RecentCompletedCourseDto recentCompletedCourse;
    private List<LearningActivityDto> learningActivities;
    private List<QuizWrongNoteDto> quizWrongNotes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyCourseDto {
        private Long courseId;
        private String courseTitle;
        private String courseType; // ONLINE, OFFLINE
        private String categoryType; // CAREER_PATH, CAREER_HIGH
        private Long applicationId;
        private String applicationNo; // YYYYMM-001 형식
        private int progressRate;
        private Long nextChapterId; // 이어보기용 챕터 ID
        private String lastPlayTimeStr; // MM분 SS초 형식
        private String learningStatus; // NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELED
        private String completionStatus; // 이수 완료 여부 (완료/미완료)
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCompletedCourseDto {
        private Long courseId;
        private String courseTitle;
        private String completedAtStr; // YYYY.MM.DD 형식
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningActivityDto {
        private String courseTitle;
        private String chapterTitle;
        private int chapterOrder;
        private String eventType; // PLAY, PAUSE, PROGRESS, COMPLETED
        private int progressRate;
        private String eventAtStr; // YYYY.MM.DD HH:mm 형식
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuizWrongNoteDto {
        private Long attemptId;
        private Long quizId;
        private Long courseId;
        private String courseTitle;
        private Long chapterId;
        private String chapterTitle;
        private int score;
        private String questionText;
        private String explanation;
        private String submittedOptionText; // 내가 선택한 오답 선지
        private String correctOptionText;   // 실제 정답 선지
        private String submittedAtStr; // YYYY.MM.DD 형식
    }
}
