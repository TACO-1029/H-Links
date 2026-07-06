package com.hlinks.domain.statistics.mapper;

import com.hlinks.domain.statistics.dto.DepartmentCompetencyScoreRow;
import com.hlinks.domain.statistics.dto.DepartmentCourseRankRow;
import com.hlinks.domain.statistics.dto.DepartmentGrowthPointRow;
import com.hlinks.domain.statistics.dto.DepartmentGrowthQuery;
import com.hlinks.domain.statistics.dto.OrganizationKpiStats;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import com.hlinks.domain.statistics.dto.StatisticsRankRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrganizationStatisticsMapper {

    OrganizationKpiStats selectOrganizationKpis(StatisticsFilter filter);

    // 부서별 전체 인원 대비 강의 신청 참여율 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectDepartmentParticipationRate(StatisticsFilter filter);

    // 부서별 강의 학습 완료율 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectDepartmentCompletionRate(StatisticsFilter filter);

    // 부서별 핵심 역량 평균 점수 차트 데이터를 조회합니다.
    List<DepartmentCompetencyScoreRow> selectDepartmentAverageCompetencyScores(StatisticsFilter filter);

    // 부서별 역량 성장률 차트 데이터를 조회합니다.
    List<DepartmentGrowthPointRow> selectDepartmentGrowthRates(DepartmentGrowthQuery query);

    // 강의 신청 참여율을 기준으로 부서 TOP5 랭킹 데이터를 조회합니다.
    List<StatisticsRankRow> selectTopParticipationDepartments(StatisticsFilter filter);

    // 부서별 강의 신청 수 기준 인기 강의 TOP5 데이터를 조회합니다.
    List<DepartmentCourseRankRow> selectPopularCoursesByDepartment(StatisticsFilter filter);
}
