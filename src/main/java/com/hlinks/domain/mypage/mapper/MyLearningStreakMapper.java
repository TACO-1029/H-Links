package com.hlinks.domain.mypage.mapper;

import com.hlinks.domain.mypage.dto.MyLearningStreakRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyLearningStreakMapper {
    List<MyLearningStreakRow> selectDailyLearningLogs(
            @Param("userId") Long userId,
            @Param("days") int days
    );
}
