package com.hlinks.domain.career.service;

import com.hlinks.domain.career.exception.CareerErrorCode;
import com.hlinks.domain.career.mapper.CareerMapper;
import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.dto.CareerSkillDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.SliceResponse;
import com.hlinks.global.response.code.ErrorResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareerServiceImpl implements CareerService {

    private final CareerMapper careerMapper;

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
    public void saveTargetSkills(Long diagnosisId, List<Long> skillIds, String userSetSkillLevel) {
        if (skillIds == null || skillIds.isEmpty()) {
            throw new BaseException(CareerErrorCode.SKILL_REQUIRED);
        }

        // 기존 매핑이 있다면 삭제하거나 그냥 바로 다중 등록 (처음 등록하는 것이므로)
        for (Long skillId : skillIds) {
            careerMapper.insertCareerTargetSkill(diagnosisId, skillId, userSetSkillLevel);
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
}
