package com.hlinks.domain.competency.service;

import com.hlinks.domain.competency.dto.CompetencyScorePolicyRow;
import com.hlinks.domain.competency.mapper.CompetencyScoreMapper;
import com.hlinks.domain.competency.policy.CompetencyScorePolicy;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.zip.CRC32;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompetencyScoreService {

    private static final String REFERENCE_TYPE_COURSE = "COURSE";
    private static final String REFERENCE_TYPE_GLOBAL_NEWS = "GLOBAL_NEWS";
    private static final String REFERENCE_TYPE_LEARNING_STREAK = "LEARNING_STREAK";
    private static final int STREAK_7_DAYS = 7;
    private static final int STREAK_30_DAYS = 30;

    private final CompetencyScorePolicy competencyScorePolicy;
    private final CompetencyScoreMapper competencyScoreMapper;

    @Transactional
    public boolean applyScore(Long userId, Long competencyId, CompetencyCalcType calcType) {
        BigDecimal scoreDelta = competencyScorePolicy.findScore(competencyId, calcType);
        if (scoreDelta == null) {
            return false;
        }

        return applyPolicy(userId, competencyId, calcType.getCode(), scoreDelta, null, null);
    }

    @Transactional
    public boolean applyScore(
            Long userId,
            Long competencyId,
            CompetencyCalcType calcType,
            String referenceType,
            Long referenceId) {

        BigDecimal scoreDelta = competencyScorePolicy.findScore(competencyId, calcType);
        if (scoreDelta == null) {
            return false;
        }

        return applyPolicy(userId, competencyId, calcType.getCode(), scoreDelta, referenceType, referenceId);
    }

    @Transactional
    public boolean applyActionScore(Long userId, CompetencyCalcType calcType) {
        List<CompetencyScorePolicyRow> policies = competencyScorePolicy.findActionPolicies(calcType);
        return applyPolicies(userId, policies, null, null);
    }

    @Transactional
    public boolean applyActionScore(
            Long userId,
            CompetencyCalcType calcType,
            String referenceType,
            Long referenceId) {

        List<CompetencyScorePolicyRow> policies = competencyScorePolicy.findActionPolicies(calcType);
        return applyPolicies(userId, policies, referenceType, referenceId);
    }

    @Transactional
    public boolean applySkillCourseCompletionScore(Long userId, Long courseId) {
        List<CompetencyScorePolicyRow> policies = competencyScorePolicy.findSkillCourseCompletionPolicies(courseId);
        return applyPolicies(userId, policies, REFERENCE_TYPE_COURSE, courseId);
    }

    @Transactional
    public boolean applyCourseCompletionActionScores(Long userId, Long courseId) {
        boolean applied = false;
        if (competencyScoreMapper.countRecommendedCourseForUser(userId, courseId) > 0) {
            applied |= applyActionScore(
                    userId,
                    CompetencyCalcType.RECOMMENDED_COURSE_COMPLETE,
                    REFERENCE_TYPE_COURSE,
                    courseId
            );
        }
        if (competencyScoreMapper.countFirstLearningSkillsForCourse(userId, courseId) > 0) {
            applied |= applyActionScore(
                    userId,
                    CompetencyCalcType.FIRST_SKILL_LEARNING,
                    REFERENCE_TYPE_COURSE,
                    courseId
            );
        }
        return applied;
    }

    @Transactional
    public boolean applyGlobalNewsClickScore(Long userId, String newsUrl) {
        if (userId == null || newsUrl == null || newsUrl.isBlank()) {
            return false;
        }
        return applyActionScore(
                userId,
                CompetencyCalcType.GLOBAL_NEWS_CLICK,
                REFERENCE_TYPE_GLOBAL_NEWS,
                toReferenceId(newsUrl)
        );
    }

    @Transactional
    public int applyLearningStreakRewards() {
        return applyLearningStreakRewards(LocalDate.now());
    }

    @Transactional
    public int applyLearningStreakRewards(LocalDate targetDate) {
        if (targetDate == null) {
            return 0;
        }

        int appliedCount = 0;
        appliedCount += applyLearningStreakReward(targetDate, STREAK_7_DAYS, CompetencyCalcType.STREAK_7);
        appliedCount += applyLearningStreakReward(targetDate, STREAK_30_DAYS, CompetencyCalcType.STREAK_30);
        return appliedCount;
    }

    private boolean applyPolicies(
            Long userId,
            List<CompetencyScorePolicyRow> policies,
            String referenceType,
            Long referenceId) {

        boolean applied = false;
        for (CompetencyScorePolicyRow policy : policies) {
            if (policy == null) {
                continue;
            }
            applied |= applyPolicy(
                    userId,
                    policy.getCompetencyId(),
                    policy.getCalcType(),
                    policy.getScoreDelta(),
                    referenceType,
                    referenceId
            );
        }
        return applied;
    }

    private boolean applyPolicy(
            Long userId,
            Long competencyId,
            String calcType,
            BigDecimal scoreDelta,
            String referenceType,
            Long referenceId) {

        if (userId == null || competencyId == null || calcType == null || scoreDelta == null) {
            return false;
        }

        if (competencyScoreMapper.countScoreHistory(
                userId,
                competencyId,
                calcType,
                referenceType,
                referenceId
        ) > 0) {
            return false;
        }

        int inserted = competencyScoreMapper.insertScoreHistory(
                userId,
                competencyId,
                calcType,
                scoreDelta,
                referenceType,
                referenceId
        );
        if (inserted == 0) {
            return false;
        }

        competencyScoreMapper.upsertUserCompetencyScore(userId, competencyId, scoreDelta);
        return true;
    }

    private int applyLearningStreakReward(LocalDate targetDate, int streakDays, CompetencyCalcType calcType) {
        List<Long> userIds = competencyScoreMapper.findLearningStreakUserIds(targetDate, streakDays);
        int appliedCount = 0;
        for (Long userId : userIds) {
            boolean applied = applyActionScore(
                    userId,
                    calcType,
                    REFERENCE_TYPE_LEARNING_STREAK,
                    userId
            );
            if (applied) {
                appliedCount++;
            }
        }
        return appliedCount;
    }

    private long toReferenceId(String value) {
        CRC32 crc32 = new CRC32();
        crc32.update(value.trim().getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }
}
