package com.hlinks.domain.course.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CourseChapterSkillMapper {

    Long findSkillIdByName(@Param("skillName") String skillName);

    int deleteByChapterId(@Param("chapterId") Long chapterId);

    int insertChapterSkill(
            @Param("chapterId") Long chapterId,
            @Param("skillId") Long skillId,
            @Param("weight") Integer weight
    );
}
