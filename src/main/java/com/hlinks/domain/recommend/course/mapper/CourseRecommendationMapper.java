package com.hlinks.domain.recommend.course.mapper;

import com.hlinks.domain.recommend.course.dto.CourseRecommendationCandidateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseRecommendationMapper {

    List<CourseRecommendationCandidateDto> findRecommendationCandidates(
            @Param("skillIds") List<Long> skillIds
    );
}
