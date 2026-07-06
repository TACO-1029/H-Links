package com.hlinks.domain.career.service;

import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.dto.CareerSkillDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.global.response.SliceResponse;

import com.hlinks.domain.career.entity.LevelTestQuestion;
import java.util.List;

public interface CareerService {

    // 사용자의 진단 내역 존재 여부 확인
    boolean hasDiagnosis(Long userId);

    // 가장 최근 진단 ID 조회
    Long findLatestDiagnosisId(Long userId);

    // 가장 최근 진단 데이터 조회
    CareerDiagnosis findLatestDiagnosis(Long userId);

    // 최초 진단 생성
    Long createDiagnosis(Long userId);

    // 목표 학습 스킬을 저장
    void saveTargetSkills(Long diagnosisId, List<Long> skillIds, List<String> userSetSkillLevels);

    // 진단에 등록된 목표 스킬 수 조회
    int getTargetSkillCount(Long diagnosisId);

    // 진단에 생성된 문제 수 조회
    int getQuestionCount(Long diagnosisId);

    // 사용자가 제출한 답안 수 조회
    int getAnswerCount(Long diagnosisId);

    // 생성된 강좌 추천 수 조회
    int getRecommendationCount(Long diagnosisId);

    // 전체 활성 스킬 목록 조회
    List<CareerSkillDto> getAllActiveSkills();

    SliceResponse<CourseListResponseDto> getRecommendationCourseSlice(Long userId, Long diagnosisId, int page, int size);

    // 비동기로 레벨 테스트 생성
    void buildLevelTestAsync(Long diagnosisId, List<Long> skillIds, List<String> difficulties);

    // 레벨 테스트 문제 목록 조회 (정답 여부 제외)
    List<LevelTestQuestion> getLevelTestQuestions(Long diagnosisId);

    // 답안 제출 및 채점
    void submitAnswers(Long diagnosisId, Long userId, List<Long> questionIds, List<Long> selectedOptionIds);

    // 진단 상세 조회
    CareerDiagnosis findDiagnosisById(Long diagnosisId);
}
