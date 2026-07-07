package com.hlinks.domain.recommend.kcy.mapper;


import com.hlinks.domain.recommend.kcy.dto.KcyOptionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyMatchCandidateDto;
import com.hlinks.domain.recommend.kcy.dto.KcyQuestionDto;
import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KcyMapper {

    // KCY 질문을 전체 가져옵니다
    List<KcyQuestionDto> findActiveQuestions();
    // KCY 질문에 대한 문항 4개를 가져옵니다
    List<KcyOptionDto> findOptionsByQuestionIds(@Param("questionIds") List<Long> questionIds);

    KcyScoreDto sumScoresByOptionIds(@Param("optionIds") List<Long> optionIds);

    int countDistinctQuestionsByOptionIds(@Param("optionIds") List<Long> optionIds);

    int updateUserKcyResult(@Param("userId") Long userId, @Param("kcyResult") String kcyResult);

    String findKcyResultByUserId(@Param("userId") Long userId);

    List<KcyMatchCandidateDto> findKcyMatchCandidates(@Param("userId") Long userId);
}
