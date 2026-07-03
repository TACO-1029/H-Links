package com.hlinks.domain.interest.mapper;

import com.hlinks.domain.interest.dto.InterestDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterestMapper {

    // 화면에 뿌릴 모든 Skill 가져오는 메서드
    List<InterestDto> findAllActiveInterests();

    // 사용자가 선택했던 관심분야(Skill) 가져오는 메서드
    List<InterestDto> findInterestsByUserId(@Param("userId") Long userId);

    // 관심분야 갯수 가져오는 메서드
    int countInterestsByUserId(@Param("userId") Long userId);

    // 삭제
    void deleteInterestsByUserId(@Param("userId") Long userId);

    // 삽입
    void insertUserInterest(@Param("userId") Long userId, @Param("interestId") Long interestId);
}
