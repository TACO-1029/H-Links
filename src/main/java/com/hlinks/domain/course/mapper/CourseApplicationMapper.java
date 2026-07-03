package com.hlinks.domain.course.mapper;

import com.hlinks.domain.course.dto.CourseApplicationListResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseApplicationMapper {

    /**
     * 사용자의 신청 내역 전체 목록 조회 (최신순)
     */
    List<CourseApplicationListResponseDto> selectApplicationListByUserId(@Param("userId") Long userId);

    /**
     * 신청 단건 상세 조회 (취소 가능 여부 검증 및 상세 모달용)
     */
    Optional<CourseApplicationListResponseDto> selectApplicationById(@Param("applicationId") Long applicationId);

    /**
     * 신청 취소 처리 (상태를 'CANCELED'로 변경하고 취소일 업데이트)
     */
    int updateApplicationToCanceled(@Param("applicationId") Long applicationId);
}