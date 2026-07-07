package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.ChapterResponseDto;
import com.hlinks.domain.course.dto.CourseApplyResponseDto;
import com.hlinks.domain.course.dto.CourseDetailResponseDto;
import com.hlinks.domain.course.dto.CourseListResponseDto;
import com.hlinks.domain.course.dto.LearningProgressResponseDto;
import com.hlinks.domain.course.dto.LearningProgressSaveRequest;
import com.hlinks.domain.course.service.CourseLearningService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final CourseLearningService courseLearningService;

    /**
     * 교육 콘텐츠 탭 - 전체 강의 목록 조회 화면
     * GET /course?categoryType=CAREER_HIGH
     */
    @GetMapping
    public String index(
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) List<String> courseTypes,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(defaultValue = "latest") String sort,
            Model model) {

        log.info("강의 목록 화면 진입 - 요청 카테고리: {}", categoryType);

        // 1. 최초 화면은 12개만 렌더링하고 이후 데이터는 무한스크롤 API로 조회합니다.
        var initialCourseSlice = courseService.getCourseSlice(
                categoryType,
                courseTypes != null ? courseTypes : List.of(),
                skillIds != null ? skillIds : List.of(),
                null,
                null,
                null,
                sort,
                0,
                12
        );
        List<CourseListResponseDto> courses = initialCourseSlice.getContent();

        // 2. Thymeleaf 템플릿(HTML)에서 접근할 수 있도록 Model에 데이터 담기
        model.addAttribute("courses", courses);
        model.addAttribute("initialHasNext", initialCourseSlice.isHasNext());
        model.addAttribute("skillGroups", courseService.getSkillFilterGroups());
        model.addAttribute("selectedCourseTypes", courseTypes != null ? courseTypes : List.of());
        model.addAttribute("selectedSkillIds", skillIds != null ? skillIds : List.of());
        model.addAttribute("selectedSort", "popular".equals(sort) ? "popular" : "latest");

        // 3. UI 처리용 부가 정보 세팅
        // 3-1. 현재 활성화된 GNB(상단 메뉴) 표시를 위한 설정 (제공해주신 예시 참고)
        model.addAttribute("activeMenu", "courses");

        // 3-2. 화면에서 현재 선택된 서브 탭(전체/커리어하이/커리어패스) CSS 활성화를 위해 담아둠
        model.addAttribute("currentCategory", categoryType);

        // 4. src/main/resources/templates/course/index.html 파일로 렌더링 포워딩
        return "course/index";
    }

    @GetMapping("/fragments/list")
    public String courseListFragment(
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) List<String> courseTypes,
            @RequestParam(required = false) List<Long> skillIds,
            @RequestParam(defaultValue = "latest") String sort,
            Model model) {

        List<CourseListResponseDto> courses = courseService.getCourseList(
                categoryType,
                courseTypes != null ? courseTypes : List.of(),
                skillIds != null ? skillIds : List.of(),
                sort
        );

        model.addAttribute("courses", courses);
        model.addAttribute("selectedSort", "popular".equals(sort) ? "popular" : "latest");

        return "course/index :: courseResults";
    }

    // ========================================================
    // [이슈 #31] 강의 상세 조회 화면 포워딩 (@PathVariable)
    // ========================================================
    /**
     * 강의 상세 조회 화면을 반환합니다.
     * GET /courses/1, GET /courses/55 형태로 진입합니다.
     */
    @GetMapping("/{courseId}")
    public String detail(@PathVariable("courseId") Long courseId, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        log.info("강의 상세 화면 진입 - 요청 강의 ID: {}", courseId);

        // 1. Service를 호출하여 검증이 완료된 온/오프라인 통합 상세 데이터 가져오기
        // (ID가 없거나 잘못된 경우 Service 내부에서 BaseException이 발생하여 전역 에러 핸들러로 빠집니다)
        CourseDetailResponseDto courseDetail = courseService.getCourseDetail(courseId, userDetails.getUserId());

        // 2. Thymeleaf가 구멍 뚫린 란에 채워 넣을 수 있도록 Model에 바인딩
        model.addAttribute("course", courseDetail);

        // 3. UI 레이아웃 유지를 위한 부가 정보 세팅
        // GNB(상단 네비게이션)의 '교육 콘텐츠' 탭을 활성화 상태로 유지하기 위한 설정
        model.addAttribute("activeMenu", "courses");

        // 4. 단 하나의 상세 템플릿(src/main/resources/templates/course/detail.html)으로 포워딩
        return "course/detail";
    }

    @GetMapping("/{courseId}/chapters/{chapterId}")
    public String chapter(
            @PathVariable("courseId") Long courseId,
            @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        log.info("온라인 강의 챕터 화면 진입 - courseId={}, chapterId={}", courseId, chapterId);

        CourseDetailResponseDto courseDetail =
                courseService.getOnlineChapterPage(courseId, chapterId, userDetails.getUserId());
        ChapterResponseDto currentChapter = courseDetail.getChapters().stream()
                .filter(chapter -> chapter.getChapterId().equals(chapterId))
                .findFirst()
                .orElseThrow();

        model.addAttribute("course", courseDetail);
        model.addAttribute("currentChapter", currentChapter);
        model.addAttribute("activeMenu", "courses");

        return "course/chapter";
    }

    @PostMapping("/{courseId}/chapters/{chapterId}/learning/start")
    @ResponseBody
    public ResponseEntity<SuccessResponse<Void>> startChapterLearning(
            @PathVariable("courseId") Long courseId,
            @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        courseLearningService.startOnlineChapterLearning(courseId, chapterId, userDetails.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.empty());
    }


    // 강의 신청
    @GetMapping("/{courseId}/chapters/{chapterId}/learning/progress")
    @ResponseBody
    public ResponseEntity<SuccessResponse<LearningProgressResponseDto>> getChapterProgress(
            @PathVariable("courseId") Long courseId,
            @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        LearningProgressResponseDto result =
                courseLearningService.getChapterProgress(courseId, chapterId, userDetails.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.from(result));
    }

    @PatchMapping("/{courseId}/chapters/{chapterId}/learning/progress")
    @ResponseBody
    public ResponseEntity<SuccessResponse<Void>> saveChapterProgress(
            @PathVariable("courseId") Long courseId,
            @PathVariable("chapterId") Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LearningProgressSaveRequest request) {

        courseLearningService.saveChapterProgress(courseId, chapterId, userDetails.getUserId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(SuccessResponse.empty());
    }

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

    // 강의 취소
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
