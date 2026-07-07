package com.hlinks.domain.competency.service;

import com.hlinks.domain.competency.dto.CompetencyScorePolicyRow;
import com.hlinks.domain.competency.mapper.CompetencyScoreMapper;
import com.hlinks.domain.competency.policy.CompetencyScorePolicy;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompetencyScoreService {

    private final CompetencyScorePolicy competencyScorePolicy;
    private final CompetencyScoreMapper competencyScoreMapper;

    @Transactional
    public boolean applyScore(Long userId, Long competencyId, CompetencyCalcType calcType) {
        BigDecimal scoreDelta = competencyScorePolicy.findScore(competencyId, calcType);
        if (scoreDelta == null) {
            return false;
        }

        return applyPolicy(userId, competencyId, calcType.getCode(), scoreDelta);
    }

    @Transactional
    public boolean applyActionScore(Long userId, CompetencyCalcType calcType) {
        List<CompetencyScorePolicyRow> policies = competencyScorePolicy.findActionPolicies(calcType);
        return applyPolicies(userId, policies);
    }

    @Transactional
    public boolean applySkillCourseCompletionScore(Long userId, Long courseId) {
        List<CompetencyScorePolicyRow> policies = competencyScorePolicy.findSkillCourseCompletionPolicies(courseId);
        return applyPolicies(userId, policies);
    }

    private boolean applyPolicies(Long userId, List<CompetencyScorePolicyRow> policies) {
        boolean applied = false;
        for (CompetencyScorePolicyRow policy : policies) {
            if (policy == null) {
                continue;
            }
            applied |= applyPolicy(userId, policy.getCompetencyId(), policy.getCalcType(), policy.getScoreDelta());
        }
        return applied;
    }

    private boolean applyPolicy(Long userId, Long competencyId, String calcType, BigDecimal scoreDelta) {
        if (userId == null || competencyId == null || calcType == null || scoreDelta == null) {
            return false;
        }

        if (competencyScoreMapper.countScoreHistory(userId, competencyId, calcType) > 0) {
            return false;
        }

        int inserted = competencyScoreMapper.insertScoreHistory(userId, competencyId, calcType, scoreDelta);
        if (inserted == 0) {
            return false;
        }

        competencyScoreMapper.upsertUserCompetencyScore(userId, competencyId, scoreDelta);
        return true;
    }
}
