package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.CourseDetailResponseDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 교육 콘텐츠 탭 - 전체 강의 목록 조회 화면
     * GET /course?categoryType=CAREER_HIGH
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String categoryType,
            Model model) {

        log.info("강의 목록 화면 진입 - 요청 카테고리: {}", categoryType);

        // 1. Service를 통해 화면에 뿌려줄 강의 데이터 리스트 조회
        List<CourseListResponseDto> courses = courseService.getCourseList(categoryType);

        // 2. Thymeleaf 템플릿(HTML)에서 접근할 수 있도록 Model에 데이터 담기
        model.addAttribute("courses", courses);

        // 3. UI 처리용 부가 정보 세팅
        // 3-1. 현재 활성화된 GNB(상단 메뉴) 표시를 위한 설정 (제공해주신 예시 참고)
        model.addAttribute("activeMenu", "courses");

        // 3-2. 화면에서 현재 선택된 서브 탭(전체/커리어하이/커리어패스) CSS 활성화를 위해 담아둠
        model.addAttribute("currentCategory", categoryType);

        // 4. src/main/resources/templates/course/index.html 파일로 렌더링 포워딩
        return "course/index";
    }

    // ========================================================
    // [이슈 #31] 강의 상세 조회 화면 포워딩 (@PathVariable)
    // ========================================================
    /**
     * 강의 상세 조회 화면을 반환합니다.
     * GET /courses/1, GET /courses/55 형태로 진입합니다.
     */
    @GetMapping("/{courseId}")
    public String detail(@PathVariable("courseId") Long courseId, Model model) {
        log.info("강의 상세 화면 진입 - 요청 강의 ID: {}", courseId);

        // 1. Service를 호출하여 검증이 완료된 온/오프라인 통합 상세 데이터 가져오기
        // (ID가 없거나 잘못된 경우 Service 내부에서 BaseException이 발생하여 전역 에러 핸들러로 빠집니다)
        CourseDetailResponseDto courseDetail = courseService.getCourseDetail(courseId);

        // 2. Thymeleaf가 구멍 뚫린 란에 채워 넣을 수 있도록 Model에 바인딩
        model.addAttribute("course", courseDetail);

        // 3. UI 레이아웃 유지를 위한 부가 정보 세팅
        // GNB(상단 네비게이션)의 '교육 콘텐츠' 탭을 활성화 상태로 유지하기 위한 설정
        model.addAttribute("activeMenu", "courses");

        // 4. 단 하나의 상세 템플릿(src/main/resources/templates/course/detail.html)으로 포워딩
        return "course/detail";
    }
}