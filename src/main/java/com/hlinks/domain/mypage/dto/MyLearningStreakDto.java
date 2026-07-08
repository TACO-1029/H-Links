package com.hlinks.domain.mypage.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MyLearningStreakDto(
        String startDate,
        String endDate,
        int activeDays,
        int totalLogs,
        int currentStreak,
        List<Day> days
) {
    @Builder
    public record Day(
            String date,
            int count
    ) {
    }
}
