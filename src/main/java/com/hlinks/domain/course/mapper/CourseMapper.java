package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseListResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {

    /**
     * 전체 강의 목록 조회 (HIDDEN 제외 및 상태 가공)
     * @param categoryType 선택적 필터링 (CAREER_HIGH, CAREER_PATH). null이거나 빈 값이면 전체 조회
     * @return 화면용 List DTO
     */
    List<CourseListResponseDto> findAllCourses(@Param("categoryType") String categoryType);

}