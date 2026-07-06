package com.hlinks.domain.course.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CourseChapterSkillMapper {

    List<String> findActiveSkillNames();

    Long findSkillIdByName(@Param("skillName") String skillName);

    int insertSkill(
            @Param("skillName") String skillName,
            @Param("skillType") String skillType
    );

    int deleteByChapterId(@Param("chapterId") Long chapterId);

    int insertChapterSkill(
            @Param("chapterId") Long chapterId,
            @Param("skillId") Long skillId,
            @Param("weight") BigDecimal weight,
            @Param("coverageLevel") String coverageLevel,
            @Param("coverageReason") String coverageReason
    );
}
