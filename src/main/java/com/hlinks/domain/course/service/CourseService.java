package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 적용으로 성능 최적화
public class CourseService {

    private final CourseMapper courseMapper;

    /**
     * 교육 콘텐츠 탭 - 전체 또는 카테고리별 강의 목록 조회
     * * @param categoryType 필터링할 카테고리 (CAREER_HIGH / CAREER_PATH). null 또는 빈 문자열("")일 경우 전체 조회.
     * @return 가상 상태(virtualStatus)가 포함된 강의 목록 DTO 리스트
     */
    public List<CourseListResponseDto> getCourseList(String categoryType) {
        log.info("강의 목록 조회 요청 - 카테고리 필터: {}", categoryType != null ? categoryType : "전체(ALL)");

        // MyBatis Mapper 호출 (동적 쿼리를 통해 필터링 및 가상 상태 계산 완료)
        List<CourseListResponseDto> courses = courseMapper.findAllCourses(categoryType);

        log.info("강의 목록 조회 완료 - 조회된 강의 수: {}건", courses.size());
        return courses;
    }

    /* * [추후 확장 포인트]
     * 다음 이슈 작업 시 강의 상세 조회 메서드가 이 자리에 추가될 예정입니다.
     * public CourseDetailResponseDto getCourseDetail(Long courseId) { ... }
     */
}