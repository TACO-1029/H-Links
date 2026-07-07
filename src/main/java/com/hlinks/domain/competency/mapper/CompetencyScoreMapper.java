package com.hlinks.domain.competency.mapper;

import com.hlinks.domain.competency.dto.CompetencyScorePolicyRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CompetencyScoreMapper {

    BigDecimal findActionPolicyScore(
            @Param("competencyId") Long competencyId,
            @Param("calcType") String calcType
    );

    List<CompetencyScorePolicyRow> findActionPolicies(@Param("calcType") String calcType);

    List<CompetencyScorePolicyRow> findSkillCourseCompletionPolicies(@Param("courseId") Long courseId);

    int countScoreHistory(
            @Param("userId") Long userId,
            @Param("competencyId") Long competencyId,
            @Param("calcType") String calcType
    );

    int insertScoreHistory(
            @Param("userId") Long userId,
            @Param("competencyId") Long competencyId,
            @Param("calcType") String calcType,
            @Param("scoreDelta") BigDecimal scoreDelta
    );

    int upsertUserCompetencyScore(
            @Param("userId") Long userId,
            @Param("competencyId") Long competencyId,
            @Param("scoreDelta") BigDecimal scoreDelta
    );
}
