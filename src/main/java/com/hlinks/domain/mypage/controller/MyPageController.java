package com.hlinks.domain.mypage.controller;

import com.hlinks.domain.interest.dto.InterestDto;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.domain.course.dto.CourseApplicationListResponseDto;
import com.hlinks.domain.course.service.CourseService;
import com.hlinks.domain.mypage.dto.MyCourseStatusResponseDto;
import com.hlinks.domain.recommend.kcy.service.KcyService;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.response.SuccessResponse;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/*
현재 하드코딩 되어있는 값은 후에 DB 조회 후 담도록 변경하겠습니다.
userDetails에서 가져올 수 있는 값은 이미 가져오고 다른 것들은 임시로 둡니다.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final KcyService kcyService;
    private final InterestService interestService;
    private final CourseService courseService; // [이슈 #44] CourseService 주입 추가

    @GetMapping("/mypage")
    public String myInfo(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("activeMenu", "mypage");
        model.addAttribute("activeSubMenu", "myInfo");

        addMyPageHeroModel(userDetails, model);

        // 아래로는 비동기 작업 처리하는 API 구현 예정입니다.
        model.addAttribute("email", "user@hlinks.co.kr");
        model.addAttribute("phone", "010-1234-5678");
        model.addAttribute("firstLoginChanged", "미완료");
        model.addAttribute("updatedAt", "2026.07.02");

        model.addAttribute("roles", List.of("임직원", "학습자"));
        List<InterestDto> interests = interestService.getUserInterests(userDetails.getUserId());
        model.addAttribute("interests", interests);

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
    // [이슈 #65] 내 수강현황 화면 조회
    // ========================================================
    /**
     * 로그인한 임직원의 내 수강현황 대시보드 페이지를 보여줍니다.
     */
    @GetMapping("/mypage/courses")
    public String myCourseStatus(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getUserId();
        log.info("마이페이지 내 수강현황 화면 요청 - 유저 ID: {}", userId);

        // 1. 대시보드 리치 데이터 조회 및 바인딩
        MyCourseStatusResponseDto statusDto = courseService.getMyCourseStatus(userId);
        model.addAttribute("statusDto", statusDto);

        // 2. 사이드바 내 서브메뉴 활성화 상태값 설정 및 조회한 카운트값 재사용
        model.addAttribute("activeMenu", "mypage");
        model.addAttribute("activeSubMenu", "myCourses");
        addMyPageHeroModel(userDetails, model, statusDto.getInProgressCount(), statusDto.getCompletedCount());

        // 3. templates/mypage/courses.html 렌더링
        return "mypage/courses";
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
        addMyPageHeroModel(userDetails, model);

        // 2. 4단계에서 CourseService에 추가했던 목록 조회 메서드 호출
        List<CourseApplicationListResponseDto> applications = courseService.getMyCourseApplicationList(userId);
        model.addAttribute("applications", applications);

        // 3. templates/mypage/applications.html 렌더링
        return "mypage/applications";
    }

    // MyPageController 내부
    @DeleteMapping("/mypage/applications/{courseId}") // URL을 RESTful하게 변경
    @ResponseBody
    public SuccessResponse<Void> cancelCourseApplication(
            @PathVariable("courseId") Long courseId, // PathVariable로 획득
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        courseService.cancelCourse(courseId, userId); // 서비스 로직 호출

        return SuccessResponse.empty();
    }

    private void addMyPageHeroModel(CustomUserDetails userDetails, Model model) {
        addMyPageHeroModel(userDetails, model, 
                courseService.getMyInProgressCourseCount(userDetails.getUserId()), 
                courseService.getMyCompletedCourseCount(userDetails.getUserId()));
    }

    private void addMyPageHeroModel(CustomUserDetails userDetails, Model model, int inProgressCount, int completedCount) {
        model.addAttribute("loginId", userDetails.getUsername());
        model.addAttribute("name", userDetails.getName());
        model.addAttribute("departmentName", userDetails.getDepartmentName());
        model.addAttribute("jobName", userDetails.getJobName());
        model.addAttribute("positionName", userDetails.getPositionName());
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("completedCount", completedCount);
    }
}
