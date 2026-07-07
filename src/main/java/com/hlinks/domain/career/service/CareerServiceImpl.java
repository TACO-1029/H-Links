package com.hlinks.domain.career.service;

import com.hlinks.domain.career.exception.CareerErrorCode;
import com.hlinks.domain.career.mapper.CareerMapper;
import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.dto.CareerSkillDto;
import com.hlinks.domain.competency.service.CompetencyScoreService;
import com.hlinks.domain.competency.type.CompetencyCalcType;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.SliceResponse;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.hlinks.domain.career.dto.CareerTargetSkillDto;
import com.hlinks.domain.career.entity.LevelTestQuestion;
import com.hlinks.domain.career.entity.LevelTestOption;
import com.hlinks.domain.career.entity.LevelTestAnswerLog;
import com.hlinks.domain.career.ai.service.AiLevelTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerServiceImpl implements CareerService {

    private static final String REFERENCE_TYPE_CAREER_DIAGNOSIS = "CAREER_DIAGNOSIS";

    private final CareerMapper careerMapper;
    private final AiLevelTestService aiLevelTestService;
    private final ObjectMapper objectMapper;
    private final CompetencyScoreService competencyScoreService;

    @Override
    public boolean hasDiagnosis(Long userId) {
        return careerMapper.countDiagnosisByUserId(userId) > 0;
    }

    @Override
    public Long findLatestDiagnosisId(Long userId) {
        Long latestId = careerMapper.findLatestDiagnosisIdByUserId(userId);
        if (latestId == null) {
            throw new BaseException(CareerErrorCode.DIAGNOSIS_NOT_FOUND);
        }
        return latestId;
    }

    @Override
    public CareerDiagnosis findLatestDiagnosis(Long userId) {
        CareerDiagnosis diagnosis = careerMapper.findLatestDiagnosisByUserId(userId);
        if (diagnosis == null) {
            throw new BaseException(CareerErrorCode.DIAGNOSIS_NOT_FOUND);
        }
        return diagnosis;
    }

    @Override
    @Transactional
    public Long createDiagnosis(Long userId) {
        CareerDiagnosis diagnosis = new CareerDiagnosis();
        diagnosis.setUserId(userId);
        diagnosis.setLlmSummary("AI가 설정된 목표 스킬을 기반으로 진단 보고서 및 레벨테스트를 준비하고 있습니다.");
        diagnosis.setLevelTestBuildStatus("PENDING");
        careerMapper.insertCareerDiagnosis(diagnosis);
        return diagnosis.getDiagnosisId();
    }

    @Override
    @Transactional
    public void saveTargetSkills(Long diagnosisId, List<Long> skillIds, List<String> userSetSkillLevels) {
        if (skillIds == null || skillIds.isEmpty()) {
            throw new BaseException(CareerErrorCode.SKILL_REQUIRED);
        }
        if (userSetSkillLevels == null || userSetSkillLevels.isEmpty() || userSetSkillLevels.size() != skillIds.size()) {
            throw new IllegalArgumentException("스킬 개수와 난이도 개수가 일치하지 않습니다.");
        }

        // 기존 매핑이 있다면 삭제하거나 그냥 바로 다중 등록 (처음 등록하는 것이므로)
        for (int  j = 0; j < skillIds.size(); j++) {
            careerMapper.insertCareerTargetSkill(diagnosisId, skillIds.get(j), userSetSkillLevels.get(j));
        }
    }

    @Override
    public int getTargetSkillCount(Long diagnosisId) {
        return careerMapper.countTargetSkillsByDiagnosisId(diagnosisId);
    }

    @Override
    public int getQuestionCount(Long diagnosisId) {
        return careerMapper.countQuestionsByDiagnosisId(diagnosisId);
    }

    @Override
    public int getAnswerCount(Long diagnosisId) {
        return careerMapper.countAnswersByDiagnosisId(diagnosisId);
    }

    @Override
    public int getRecommendationCount(Long diagnosisId) {
        return careerMapper.countRecommendationsByDiagnosisId(diagnosisId);
    }

    @Override
    public List<CareerSkillDto> getAllActiveSkills() {
        return careerMapper.findAllActiveSkills();
    }

    @Override
    public SliceResponse<CourseListResponseDto> getRecommendationCourseSlice(Long userId, Long diagnosisId, int page, int size) {
        if (careerMapper.countDiagnosisByDiagnosisIdAndUserId(diagnosisId, userId) == 0) {
            throw new BaseException(ErrorResponseCode.FORBIDDEN, "조회할 수 없는 커리어패스 추천 결과입니다.");
        }

        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSliceSize(size);
        int limitPlusOne = normalizedSize + 1;
        int offset = normalizedPage * normalizedSize;

        List<CourseListResponseDto> rows =
                careerMapper.findRecommendationCourseSlice(diagnosisId, offset, limitPlusOne);
        boolean hasNext = rows.size() > normalizedSize;
        List<CourseListResponseDto> content = hasNext ? rows.subList(0, normalizedSize) : rows;

        return SliceResponse.of(content, normalizedPage, normalizedSize, hasNext);
    }

    private int normalizeSliceSize(int size) {
        if (size <= 0) {
            return 12;
        }
        return Math.min(size, 50);
    }

    @Override
    @Transactional
    public void buildLevelTestAsync(Long diagnosisId, List<Long> skillIds, List<String> difficulties) {
        aiLevelTestService.buildLevelTestAsync(diagnosisId, skillIds, difficulties);
    }

    @Override
    public List<LevelTestQuestion> getLevelTestQuestions(Long diagnosisId) {
        return careerMapper.findQuestionsByDiagnosisId(diagnosisId);
    }

    @Override
    public CareerDiagnosis findDiagnosisById(Long diagnosisId) {
        return careerMapper.findDiagnosisById(diagnosisId);
    }

    @Override
    @Transactional
    public void submitAnswers(Long diagnosisId, Long userId, List<Long> questionIds, List<Long> selectedOptionIds) {
        CareerDiagnosis diagnosis = careerMapper.findDiagnosisById(diagnosisId);
        if (diagnosis == null || !diagnosis.getUserId().equals(userId)) {
            throw new BaseException(CareerErrorCode.DIAGNOSIS_NOT_FOUND);
        }

        if (questionIds == null || selectedOptionIds == null || questionIds.size() != selectedOptionIds.size()) {
            throw new IllegalArgumentException("제출된 문항과 답안의 개수가 일치하지 않습니다.");
        }

        List<LevelTestQuestion> questions = careerMapper.findQuestionsWithAnswersByDiagnosisId(diagnosisId);
        Map<Long, LevelTestQuestion> questionMap = new HashMap<>();
        for (LevelTestQuestion q : questions) {
            questionMap.put(q.getLevelQuestionId(), q);
        }

        Map<Long, Boolean> answerResults = new HashMap<>(); // Question ID -> Correct (true/false)

        for (int i = 0; i < questionIds.size(); i++) {
            Long qId = questionIds.get(i);
            Long selectedOptId = selectedOptionIds.get(i);

            LevelTestQuestion q = questionMap.get(qId);
            if (q == null) continue;

            boolean isCorrect = false;
            if (q.getOptions() != null) {
                for (LevelTestOption opt : q.getOptions()) {
                    if (opt.getLevelOptionId().equals(selectedOptId) && "Y".equals(opt.getCorrectYn())) {
                        isCorrect = true;
                        break;
                    }
                }
            }

            answerResults.put(qId, isCorrect);

            LevelTestAnswerLog logEntity = new LevelTestAnswerLog();
            logEntity.setUserId(userId);
            logEntity.setLevelQuestionId(qId);
            logEntity.setSelectedOptionId(selectedOptId);
            logEntity.setCorrectYn(isCorrect ? "Y" : "N");
            careerMapper.insertLevelTestAnswerLog(logEntity);
        }

        // Calculate score per skill using normalized deduction formula
        List<CareerTargetSkillDto> targetSkills = careerMapper.findTargetSkillsByDiagnosisId(diagnosisId);
        String categoryName = careerMapper.getCategoryNameByDiagnosisId(diagnosisId);
        if (categoryName == null) {
            categoryName = "IT 기술";
        }

        List<Map<String, Object>> resultsList = new ArrayList<>();

        for (CareerTargetSkillDto targetSkill : targetSkills) {
            Long skillId = targetSkill.getSkillId();
            String userDiff = targetSkill.getDifficulty();
            if (userDiff == null) userDiff = "MEDIUM";
            if ("하".equals(userDiff)) userDiff = "LOW";
            else if ("상".equals(userDiff)) userDiff = "HIGH";
            else if ("중".equals(userDiff)) userDiff = "MEDIUM";
            userDiff = userDiff.toUpperCase();

            double totalWeightSum = 0.0;
            double wrongWeightSum = 0.0;
            int skillQuestionCount = 0;

            for (LevelTestQuestion q : questions) {
                if (q.getSkillId().equals(skillId)) {
                    skillQuestionCount++;
                    String qDiff = q.getDifficulty();
                    if (qDiff == null) qDiff = "MEDIUM";
                    qDiff = qDiff.toUpperCase();

                    double weight = 1.0;
                    if ("LOW".equals(userDiff)) {
                        if ("LOW".equals(qDiff)) weight = 1.0;
                        else if ("MEDIUM".equals(qDiff)) weight = 0.5;
                        else if ("HIGH".equals(qDiff)) weight = 0.1;
                    } else if ("MEDIUM".equals(userDiff)) {
                        if ("LOW".equals(qDiff)) weight = 1.5;
                        else if ("MEDIUM".equals(qDiff)) weight = 1.0;
                        else if ("HIGH".equals(qDiff)) weight = 0.5;
                    } else if ("HIGH".equals(userDiff)) {
                        if ("LOW".equals(qDiff)) weight = 2.0;
                        else if ("MEDIUM".equals(qDiff)) weight = 1.5;
                        else if ("HIGH".equals(qDiff)) weight = 1.0;
                    }

                    totalWeightSum += weight;
                    Boolean isCorrect = answerResults.get(q.getLevelQuestionId());
                    if (isCorrect != null && !isCorrect) {
                        wrongWeightSum += weight;
                    }
                }
            }

            int finalScore = 100;
            if (skillQuestionCount > 0 && totalWeightSum > 0.0) {
                finalScore = (int) Math.round(100.0 - (100.0 * wrongWeightSum / totalWeightSum));
                if (finalScore < 0) finalScore = 0;
                if (finalScore > 100) finalScore = 100;
            }

            Map<String, Object> skillResult = new HashMap<>();
            skillResult.put("skillId", skillId);
            skillResult.put("selectedDifficulty", userDiff);
            skillResult.put("score", finalScore);
            resultsList.add(skillResult);
        }

        // Generate AI feedback summary narrative
        StringBuilder scoreInfo = new StringBuilder();
        for (Map<String, Object> res : resultsList) {
            scoreInfo.append(String.format("- 기술 ID: %s, 선택 난이도: %s, 획득 점수: %s점\n",
                    res.get("skillId"), res.get("selectedDifficulty"), res.get("score")));
        }
        String aiFeedback = aiLevelTestService.generateFeedbackSummary(categoryName, scoreInfo.toString());

        Map<String, Object> finalJsonMap = new HashMap<>();
        finalJsonMap.put("category", categoryName);
        finalJsonMap.put("aiSummary", aiFeedback);
        finalJsonMap.put("results", resultsList);

        try {
            String resultJson = objectMapper.writeValueAsString(finalJsonMap);
            log.info("레벨 테스트 채점 결과 JSON 도출: {}", resultJson);

            // Persist the result in LLM Summary for next steps / dashboard
            careerMapper.updateLlmSummary(diagnosisId, resultJson);
            competencyScoreService.applyActionScore(
                    userId,
                    CompetencyCalcType.LEVEL_TEST_TAKEN,
                    REFERENCE_TYPE_CAREER_DIAGNOSIS,
                    diagnosisId
            );
        } catch (Exception e) {
            log.error("Failed to serialize scoring result JSON", e);
            throw new RuntimeException("결과 데이터 저장 중 오류가 발생했습니다.", e);
        }
    }
}
