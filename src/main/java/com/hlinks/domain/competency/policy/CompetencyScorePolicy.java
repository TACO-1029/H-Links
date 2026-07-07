package com.hlinks.domain.competency.policy;

import com.hlinks.domain.competency.dto.CompetencyScorePolicyRow;
import com.hlinks.domain.competency.mapper.CompetencyScoreMapper;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CompetencyScorePolicy {

    private final CompetencyScoreMapper competencyScoreMapper;

    public BigDecimal findScore(Long competencyId, CompetencyCalcType calcType) {
        if (competencyId == null || calcType == null) {
            return null;
        }
        return competencyScoreMapper.findActionPolicyScore(competencyId, calcType.getCode());
    }

    public List<CompetencyScorePolicyRow> findActionPolicies(CompetencyCalcType calcType) {
        if (calcType == null) {
            return List.of();
        }
        return competencyScoreMapper.findActionPolicies(calcType.getCode());
    }

    public List<CompetencyScorePolicyRow> findSkillCourseCompletionPolicies(Long courseId) {
        if (courseId == null) {
            return List.of();
        }
        return competencyScoreMapper.findSkillCourseCompletionPolicies(courseId);
    }
}
