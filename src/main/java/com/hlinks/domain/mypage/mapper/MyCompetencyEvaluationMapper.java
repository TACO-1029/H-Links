package com.hlinks.domain.mypage.mapper;

import com.hlinks.domain.mypage.dto.MyCompetencyEvaluationDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyCompetencyEvaluationMapper {

    List<MyCompetencyEvaluationDto.CompetencyScoreDto> findCompetencyScores(@Param("userId") Long userId);

    String findLastCalculatedAt(@Param("userId") Long userId);

    List<MyCompetencyEvaluationDto.ScoreHistoryDto> findRecentHistories(@Param("userId") Long userId);

    List<MyCompetencyEvaluationDto.GrowthFactorDto> findGrowthFactors(@Param("userId") Long userId);

    List<MyCompetencyEvaluationDto.ActionPlanDto> findRecommendedCoursesForWeakCompetencies(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );
}
