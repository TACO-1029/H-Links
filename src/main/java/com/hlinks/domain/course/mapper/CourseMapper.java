package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseDetailResponseDto;
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

    /**
     * 강의 상세 조회 (부모 + 온라인/오프라인 자식 테이블 LEFT JOIN)
     * @param courseId 조회할 강의 고유 ID
     * @return 가공된 상세 정보 DTO (존재하지 않으면 null 반환)
     */
    CourseDetailResponseDto findCourseDetailById(@Param("courseId") Long courseId);
}