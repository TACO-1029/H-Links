package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseSkillAggregationRow;
import com.hlinks.domain.course.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSkillAggregationService {

    private static final int TOTAL_WEIGHT = 100;

    private final CourseMapper courseMapper;

    @Transactional
    public void recalculateCourseSkills(Long courseId) {
        List<CourseSkillAggregationRow> chapterSkills = courseMapper.findChapterSkillsByCourseId(courseId);

        courseMapper.deleteCourseSkillsByCourseId(courseId);

        if (chapterSkills == null || chapterSkills.isEmpty()) {
            log.info("강의 스킬 집계 생략. CHAPTER_SKILL 데이터가 없습니다. courseId={}", courseId);
            return;
        }

        Map<Long, Integer> weights = calculateCourseSkillWeights(chapterSkills);

        if (weights.isEmpty()) {
            log.info("강의 스킬 집계 생략. 집계 가능한 스킬이 없습니다. courseId={}", courseId);
            return;
        }

        weights.forEach((skillId, weight) ->
                courseMapper.insertCourseSkill(courseId, skillId, weight)
        );
    }

    private Map<Long, Integer> calculateCourseSkillWeights(List<CourseSkillAggregationRow> chapterSkills) {
        Map<Long, Integer> chapterDurations = chapterSkills.stream()
                .collect(Collectors.toMap(
                        CourseSkillAggregationRow::getChapterId,
                        row -> row.getDurationSeconds() == null ? 0 : row.getDurationSeconds(),
                        Math::max,
                        LinkedHashMap::new
                ));

        int totalDuration = chapterDurations.values().stream()
                .filter(duration -> duration != null && duration > 0)
                .mapToInt(Integer::intValue)
                .sum();
        boolean useDurationWeight = totalDuration > 0
                && chapterDurations.values().stream().allMatch(duration -> duration != null && duration > 0);

        Map<Long, Double> rawScores = new LinkedHashMap<>();
        int chapterCount = Math.max(1, chapterDurations.size());

        for (CourseSkillAggregationRow row : chapterSkills) {
            if (row.getSkillId() == null) {
                continue;
            }

            double chapterRatio = resolveChapterRatio(row, chapterCount, totalDuration, useDurationWeight);
            double score = normalizeChapterSkillWeight(row.getWeight()) * chapterRatio;
            rawScores.merge(row.getSkillId(), score, Double::sum);
        }

        return normalizeToTotalWeight(rawScores);
    }

    private double resolveChapterRatio(
            CourseSkillAggregationRow row,
            int chapterCount,
            int totalDuration,
            boolean useDurationWeight
    ) {
        if (!useDurationWeight) {
            return 1.0 / chapterCount;
        }

        return (double) row.getDurationSeconds() / totalDuration;
    }

    private int normalizeChapterSkillWeight(Integer weight) {
        if (weight == null) {
            return 1;
        }

        return Math.max(1, weight);
    }

    private Map<Long, Integer> normalizeToTotalWeight(Map<Long, Double> rawScores) {
        double rawTotal = rawScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (rawScores.isEmpty() || rawTotal <= 0) {
            return Map.of();
        }

        List<WeightedSkill> weightedSkills = new ArrayList<>();
        int floorSum = 0;

        for (Map.Entry<Long, Double> entry : rawScores.entrySet()) {
            double normalized = entry.getValue() / rawTotal * TOTAL_WEIGHT;
            int floorWeight = (int) Math.floor(normalized);
            floorSum += floorWeight;
            weightedSkills.add(new WeightedSkill(entry.getKey(), floorWeight, normalized - floorWeight));
        }

        int remainingWeight = TOTAL_WEIGHT - floorSum;
        weightedSkills.stream()
                .sorted(Comparator.comparingDouble(WeightedSkill::remainder).reversed())
                .limit(remainingWeight)
                .forEach(WeightedSkill::increaseWeight);

        return weightedSkills.stream()
                .collect(Collectors.toMap(
                        WeightedSkill::skillId,
                        WeightedSkill::weight,
                        Integer::sum,
                        LinkedHashMap::new
                ));
    }

    private static class WeightedSkill {
        private final Long skillId;
        private int weight;
        private final double remainder;

        private WeightedSkill(Long skillId, int weight, double remainder) {
            this.skillId = skillId;
            this.weight = weight;
            this.remainder = remainder;
        }

        private Long skillId() {
            return skillId;
        }

        private int weight() {
            return weight;
        }

        private double remainder() {
            return remainder;
        }

        private void increaseWeight() {
            this.weight += 1;
        }
    }
}
