package com.hlinks.domain.mypage.service;

import com.hlinks.domain.mypage.dto.MyLearningStreakDto;
import com.hlinks.domain.mypage.dto.MyLearningStreakRow;
import com.hlinks.domain.mypage.mapper.MyLearningStreakMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyLearningStreakServiceImpl implements MyLearningStreakService {

    private static final int RANGE_DAYS = 120;

    private final MyLearningStreakMapper myLearningStreakMapper;

    @Override
    public MyLearningStreakDto getLearningStreak(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(RANGE_DAYS - 1L);
        List<MyLearningStreakRow> rows = myLearningStreakMapper.selectDailyLearningLogs(userId, RANGE_DAYS);
        Map<String, Integer> countByDate = rows.stream()
                .collect(Collectors.toMap(MyLearningStreakRow::getLearningDate, MyLearningStreakRow::getLogCount));

        List<MyLearningStreakDto.Day> days = new ArrayList<>();
        int activeDays = 0;
        int totalLogs = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String key = date.toString();
            int count = countByDate.getOrDefault(key, 0);
            if (count > 0) {
                activeDays++;
                totalLogs += count;
            }
            days.add(MyLearningStreakDto.Day.builder()
                    .date(key)
                    .count(count)
                    .build());
        }

        return MyLearningStreakDto.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .activeDays(activeDays)
                .totalLogs(totalLogs)
                .currentStreak(calculateCurrentStreak(days))
                .days(days)
                .build();
    }

    private int calculateCurrentStreak(List<MyLearningStreakDto.Day> days) {
        int streak = 0;
        for (int index = days.size() - 1; index >= 0; index--) {
            if (days.get(index).count() == 0) {
                break;
            }
            streak++;
        }
        return streak;
    }
}
