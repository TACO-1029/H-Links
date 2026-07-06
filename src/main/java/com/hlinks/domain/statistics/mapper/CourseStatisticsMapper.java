package com.hlinks.domain.statistics.mapper;

import com.hlinks.domain.statistics.dto.CoursePeriodQuery;
import com.hlinks.domain.statistics.dto.CoursePeriodSeriesRow;
import com.hlinks.domain.statistics.dto.PopularCourseRow;
import com.hlinks.domain.statistics.dto.SkillPopularityChangeQuery;
import com.hlinks.domain.statistics.dto.StatisticsFilter;
import com.hlinks.domain.statistics.dto.StatisticsPointRow;
import com.hlinks.domain.statistics.dto.StatisticsRankRow;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CourseStatisticsMapper {

    // ========== KPI ==========
    // 선택한 기간에 개설된 강의 수를 조회합니다.
    BigDecimal countCreatedCourses(StatisticsFilter filter);

    // 현재 운영 중인 강의 수를 조회합니다.
    BigDecimal countOperatingCourses(StatisticsFilter filter);

    // 선택한 기간의 수강 신청 건수를 조회합니다.
    BigDecimal countCourseApplications(StatisticsFilter filter);

    // 선택한 조건의 강의 학습 이력 중 완료 상태 비율을 조회합니다.
    BigDecimal selectCourseCompletionRate(StatisticsFilter filter);

    // 선택한 조건의 강의 학습 이력 중 평균 진도율을 조회합니다.
    BigDecimal selectAverageProgressRate(StatisticsFilter filter);

    // 현재 운영 중인 강의 수를 강의 유형별로 집계합니다.
    List<StatisticsPointRow> selectOperatingCoursesByType(StatisticsFilter filter);

    // 강의 유형별로 학습 완료율을 집계해 온라인/오프라인 비교 차트 데이터를 조회합니다.
    List<StatisticsPointRow> selectCourseTypeCompletionRate(StatisticsFilter filter);

    // 신청 수를 기준으로 인기 강의 TOP5 랭킹 데이터를 조회합니다.
    List<PopularCourseRow> selectPopularCourses(StatisticsFilter filter);

    // 미수료 또는 이탈 신청 건수를 기준으로 강의 TOP5 랭킹 데이터를 조회합니다.
    List<StatisticsRankRow> selectIncompleteCourses(StatisticsFilter filter);

    // 선택 기간의 수강 신청 수를 기준으로 세부 스킬 TOP5 데이터를 조회합니다.
    List<StatisticsPointRow> selectPopularSkills(StatisticsFilter filter);

    // 이전 동일 기간 대비 신청 수 증가가 큰 급상승 세부 스킬 TOP5 데이터를 조회합니다.
    List<StatisticsPointRow> selectRisingSkills(SkillPopularityChangeQuery query);

    // 기간 단위별 수강 신청 및 수료 추이 데이터를 조회합니다.
    List<CoursePeriodSeriesRow> selectApplicationCompletionTrend(CoursePeriodQuery query);

    // 기간 단위별 수강 신청 대비 수료 전환율 추이 데이터를 조회합니다.
    List<StatisticsPointRow> selectApplicationCompletionConversionTrend(CoursePeriodQuery query);
}
