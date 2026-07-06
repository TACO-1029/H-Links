package com.hlinks.domain.statistics.dto;

public record RankStatDto(
        int rank,
        String badgeText,
        String badgeTone,
        String label,
        String value,
        String categoryName,
        String courseTypeName,
        String applicationCount,
        String completionRate,
        String averageProgressRate,
        String quizCorrectRate
) {
    public RankStatDto(int rank, String badgeText, String badgeTone, String label, String value) {
        this(rank, badgeText, badgeTone, label, value, null, null, null, null, null, null);
    }

    public boolean hasCourseDetails() {
        return categoryName != null;
    }
}
