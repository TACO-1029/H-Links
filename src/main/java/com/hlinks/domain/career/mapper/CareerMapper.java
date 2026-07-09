package com.hlinks.domain.career.mapper;

import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.dto.CareerSkillDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.career.dto.CareerTargetSkillDto;
import com.hlinks.domain.career.entity.LevelTestQuestion;
import com.hlinks.domain.career.entity.LevelTestOption;
import com.hlinks.domain.career.entity.LevelTestAnswerLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CareerMapper {

    // 사용자의 커리어 진단 건수 조회
    int countDiagnosisByUserId(@Param("userId") Long userId);

    // 가장 최근 진단 ID 조회
    Long findLatestDiagnosisIdByUserId(@Param("userId") Long userId);

    // 가장 최근 진단 상세 조회
    CareerDiagnosis findLatestDiagnosisByUserId(@Param("userId") Long userId);

    int countDiagnosisByDiagnosisIdAndUserId(
            @Param("diagnosisId") Long diagnosisId,
            @Param("userId") Long userId
    );

    // 신규 커리어 진단 데이터 삽입
    void insertCareerDiagnosis(CareerDiagnosis diagnosis);

    // 목표 스킬 매핑 데이터 삽입
    void insertCareerTargetSkill(
            @Param("diagnosisId") Long diagnosisId,
            @Param("skillId") Long skillId,
            @Param("userSetSkillLevel") String userSetSkillLevel
    );

    // 진단에 생성된 문제 수 조회
    int countQuestionsByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 사용자가 제출한 답안 수 조회
    int countAnswersByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 생성된 강좌 추천 수 조회
    int countRecommendationsByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 진단에 등록된 목표 스킬 수 조회
    int countTargetSkillsByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 전체 활성 스킬 및 카테고리 정보 조회
    List<CareerSkillDto> findAllActiveSkills();

    List<CourseListResponseDto> findRecommendationCourseSlice(
            @Param("diagnosisId") Long diagnosisId,
            @Param("offset") int offset,
            @Param("limitPlusOne") int limitPlusOne
    );

    // 진단 상세 조회
    CareerDiagnosis findDiagnosisById(@Param("diagnosisId") Long diagnosisId);

    // AI 시험지 생성 상태 업데이트
    void updateLevelTestBuildStatus(@Param("diagnosisId") Long diagnosisId, @Param("status") String status);

    // AI 진단 총평(LLM Summary) 업데이트
    void updateLlmSummary(@Param("diagnosisId") Long diagnosisId, @Param("llmSummary") String llmSummary);

    // 레벨 테스트 문항 저장
    void insertLevelTestQuestion(LevelTestQuestion question);

    // 레벨 테스트 보기 옵션 저장
    void insertLevelTestOption(LevelTestOption option);

    // 레벨 테스트 문항 목록 조회 (정답 제외)
    List<LevelTestQuestion> findQuestionsByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 레벨 테스트 문항 및 정답 포함 조회 (채점용)
    List<LevelTestQuestion> findQuestionsWithAnswersByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 답안 제출 기록 저장
    void insertLevelTestAnswerLog(LevelTestAnswerLog answerLog);

    // 스킬 ID로 스킬명 조회
    String getSkillNameById(@Param("skillId") Long skillId);

    // 진단에 등록된 목표 스킬 및 난이도 조회
    List<CareerTargetSkillDto> findTargetSkillsByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 진단의 핵심 카테고리명 조회
    String getCategoryNameByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    // 사용자의 부서 연관 스킬 ID 목록 조회
    List<Long> findDepartmentSkillIdsByUserId(@Param("userId") Long userId);

    // 사용자의 부서 연관 스킬들의 상위 카테고리 ID 목록 조회
    List<Long> findDepartmentCategoryIdsByUserId(@Param("userId") Long userId);
}
