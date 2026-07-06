package com.hlinks.domain.career.mapper;

import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.dto.CareerSkillDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
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
}
