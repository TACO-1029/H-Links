package com.hlinks.domain.mypage.controller;

import com.hlinks.domain.course.dto.CourseApplicationListResponseDto;
import com.hlinks.domain.course.service.CourseService;
import com.hlinks.domain.recommend.kcy.service.KcyService;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.response.SuccessResponse;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


/*
현재 하드코딩 되어있는 값은 후에 DB 조회 후 담도록 변경하겠습니다.
userDetails에서 가져올 수 있는 값은 이미 가져오고 다른 ㄱㅓㅅ들은 임시로 둡니다.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final KcyService kcyService;
    private final CourseService courseService; // [이슈 #44] CourseService 주입 추가

    @GetMapping("/mypage")
    public String myInfo(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("activeMenu", "mypage");
        model.addAttribute("activeSubMenu", "myInfo");

        model.addAttribute("loginId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getName());
        model.addAttribute("departmentName", userDetails.getDepartmentName());
        model.addAttribute("jobName", userDetails.getJobName());
        model.addAttribute("positionName", userDetails.getPositionName());

        // 아래로는 비동기 작업 처리하는 API 구현 예정입니다.
        model.addAttribute("email", "user@hlinks.co.kr");
        model.addAttribute("phone", "010-1234-5678");
        model.addAttribute("firstLoginChanged", "미완료");
        model.addAttribute("updatedAt", "2026.07.02");

        model.addAttribute("inProgressCount", 1);
        model.addAttribute("completedCount", 1);

        model.addAttribute("roles", List.of("임직원", "학습자"));
        model.addAttribute("interests", List.of("프론트엔드", "클라우드", "AI 자동화"));

        // KCY 검사 하드코딩 되어있던 부분 수정
        KcyType kcyResult = kcyService.getResult(userDetails.getUserId());
        model.addAttribute("hasKcyResult", kcyResult != null);

        if (kcyResult != null) {
            model.addAttribute("kcyCode", kcyResult.getCode());
            model.addAttribute("kcyTitle", kcyResult.getTitle());
            model.addAttribute("kcyDescription", kcyResult.getDescription());
            model.addAttribute("kcyImagePath", kcyResult.getImagePath());
        }

        model.addAttribute("careerDate", "2026.06.18");
        model.addAttribute("careerTitle", "프론트엔드 리드 트랙");
        model.addAttribute("careerDescription", "UI 아키텍처와 클라우드 기반 배포 역량을 함께 강화하면 제품형 엔지니어로 성장 가능성이 높습니다.");

        return "mypage/info";
    }

    // ========================================================
    // [APP-001] 내 강의 신청 내역 화면 조회 및 일정 연동
    // ========================================================
    /**
     * 로그인한 임직원의 강의/특강 신청 내역 목록 페이지를 보여줍니다.
     */
    @GetMapping("/mypage/applications")
    public String myCourseApplications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUserId();
        log.info("마이페이지 내 신청 내역 화면 요청 - 유저 ID: {}", userId);

        // 1. 사이드바 내 서브메뉴 활성화 상태값 설정
        model.addAttribute("activeMenu", "mypage");
        model.addAttribute("activeSubMenu", "myApplications");

        // 2. 4단계에서 CourseService에 추가했던 목록 조회 메서드 호출
        List<CourseApplicationListResponseDto> applications = courseService.getMyCourseApplicationList(userId);
        model.addAttribute("applications", applications);

        // 3. templates/mypage/applications.html 렌더링
        return "mypage/applications";
    }

    // ========================================================
    // [APP-004] 신청 취소 기능 연동 (팀원 구현 로직 재활용)
    // ========================================================
    /**
     * 대기 상태 혹은 신청 기간 내의 강의 신청 건을 취소 처리합니다.
     * 비동기(Fetch API/Axios) 호출에 대응하여 JSON 공통 스펙으로 응답합니다.
     */
    @PostMapping("/mypage/applications/cancel")
    @ResponseBody
    public SuccessResponse<Void> cancelCourseApplication(
            @RequestParam("courseId") Long courseId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        log.info("마이페이지 신청 취소 API 호출 - 강의 ID: {}, 유저 ID: {}", courseId, userId);

        // 다른 팀원이 미리 구현해 둔 검증 및 차감 로직이 포함된 서비스 메서드 연동
        courseService.cancelCourse(courseId, userId);

        // 내부적으로 new SuccessResponse<>(null, SuccessResponseCode.SUCCESS_OK)를 수행해줍니다.
        return SuccessResponse.empty();
    }
}
