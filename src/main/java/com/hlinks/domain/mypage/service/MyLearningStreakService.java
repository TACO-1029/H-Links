package com.hlinks.domain.mypage.service;

import com.hlinks.domain.mypage.dto.MyLearningStreakDto;

public interface MyLearningStreakService {
    MyLearningStreakDto getLearningStreak(Long userId);
}
