package com.hlinks.domain.course.service;

import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.mapper.CourseMapper;
import com.hlinks.global.exception.BaseException; // H-Link 공통 예외
import com.hlinks.global.response.code.ErrorResponseCode; // 에러 코드 ENUM (경로/이름은 실제에 맞게 조정)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseMapper courseMapper;

    public List<CourseListResponseDto> getCourseList(String categoryType) {
        log.info("강의 목록 조회 요청 - 카테고리 필터: {}", categoryType != null ? categoryType : "전체(ALL)");

        // ==========================================
        // [수정된 부분] BaseException 활용 방어 로직
        // ==========================================
        if (categoryType != null && !categoryType.trim().isEmpty()) {
            if (!categoryType.equals("CAREER_HIGH") && !categoryType.equals("CAREER_PATH")) {
                log.warn("유효하지 않은 카테고리 파라미터 접근: {}", categoryType);
                // 잘못된 파라미터가 들어오면 커스텀 예외를 던집니다.
                // (GlobalExceptionHandler가 이를 낚아채어 ErrorResponse로 프론트에 전달해 줍니다)
                throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER);
            }
        }

        List<CourseListResponseDto> courses = courseMapper.findAllCourses(categoryType);

        // 목록 조회의 경우 데이터가 0건(Empty)인 것은 에러가 아니므로 정상 반환합니다.
        // 추후 '상세 조회(detail)' 시 조회된 결과가 null이면 여기서 BaseException(COURSE_NOT_FOUND)을 던지게 됩니다.

        log.info("강의 목록 조회 완료 - 조회된 강의 수: {}건", courses.size());
        return courses;
    }
}