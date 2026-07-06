package com.hlinks.domain.statistics.mapper;

import com.hlinks.domain.statistics.dto.LearningKpiStats;
import com.hlinks.domain.statistics.dto.DepartmentGrowthQuery;
import com.hlinks.domain.statistics.dto.KcyParticipationRow;
import com.hlinks.domain.statistics.dto.LearningPeriodQuery;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LearningStatisticsMapper {

    // ========== KPI ==========
    // 선택한 조건의 학습 KPI와 대상 임직원 수를 한 번에 조회합니다.
    LearningKpiStats selectLearningKpis(StatisticsFilter filter);

    // ========== 학습 활동 ==========
    // 학습 로그를 시간대별로 집계해 시간별 학습자 패턴 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectHourlyLearningPattern(StatisticsFilter filter);

    // 학습 로그를 요일별로 집계해 요일별 학습자 패턴 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectWeekdayLearningPattern(StatisticsFilter filter);

    // 학습 로그의 재생 시간을 기간 단위로 합산해 학습 시간 추이 데이터를 조회합니다.
    List<StatisticsPointRow> selectLearningHoursInPeriod(LearningPeriodQuery query);

    // ========== 참여 분석 ==========
    // 전체 임직원 대비 신청, 학습 시작, 수료 단계별 참여율을 조회합니다.
    List<StatisticsPointRow> selectLearningParticipationFunnel(StatisticsFilter filter);

    // 선택 기간 내 학습 참여 사용자를 강의 유형별 KCY TOP5 참여 비중으로 조회합니다.
    List<KcyParticipationRow> selectTopKcyParticipationByCourseType(StatisticsFilter filter);

    // ========== 성장 분석 ==========
    // 사용자의 역량 점수를 역량 항목별로 평균 내어 레이더 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectAverageCompetencyScores(StatisticsFilter filter);

    List<StatisticsPointRow> selectCompanyAverageCompetencyScores(StatisticsFilter filter);

    // 역량 점수 이력을 선택 기간 기준 단위로 평균 내어 역량 성장 추이 데이터를 조회합니다.
    List<StatisticsPointRow> selectCompetencyGrowthInPeriod(DepartmentGrowthQuery query);

    List<StatisticsPointRow> selectCompletedCourseCountsInPeriod(LearningPeriodQuery query);
}
