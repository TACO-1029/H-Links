package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.CourseApplyResponseDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.service.CourseService;
import com.hlinks.global.response.SuccessResponse;
import com.hlinks.global.response.code.SuccessResponseCode;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
        model.addAttribute("activeMenu", "course");

        // 3-2. 화면에서 현재 선택된 서브 탭(전체/커리어하이/커리어패스) CSS 활성화를 위해 담아둠
        model.addAttribute("currentCategory", categoryType);

        // 4. src/main/resources/templates/course/index.html 파일로 렌더링 포워딩
        return "course/index";
    }

    /*
    강의 신청
    */
    @PostMapping("/{courseId}/applications")
    @ResponseBody
    public ResponseEntity<SuccessResponse<CourseApplyResponseDto>> applyCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CourseApplyResponseDto result = courseService.applyCourse(courseId, userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SuccessResponse.of(result, SuccessResponseCode.SUCCESS_CREATED));
    }

    @DeleteMapping("/{courseId}/applications")
    @ResponseBody
    public ResponseEntity<SuccessResponse<Void>> cancelCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        courseService.cancelCourse(courseId, userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.empty());
    }
}
