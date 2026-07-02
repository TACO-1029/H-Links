package com.hlinks.domain.mypage.controller;

import com.hlinks.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


/*
현재 하드코딩 되어있는 값은 후에 DB 조회 후 담도록 변경하겠습니다.
userDetails에서 가져올 수 있는 값은 이미 가져오고 다른 ㄱㅓㅅ들은 임시로 둡니다.
 */
@Controller
public class MyPageController {

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

        model.addAttribute("kcyDate", "2026.06.12");
        model.addAttribute("kcyTitle", "실행형 문제 해결가");
        model.addAttribute("kcyDescription", "실험과 검증을 빠르게 반복하며, 팀의 목표를 실제 결과물로 연결하는 성향이 강합니다.");

        model.addAttribute("careerDate", "2026.06.18");
        model.addAttribute("careerTitle", "프론트엔드 리드 트랙");
        model.addAttribute("careerDescription", "UI 아키텍처와 클라우드 기반 배포 역량을 함께 강화하면 제품형 엔지니어로 성장 가능성이 높습니다.");

        return "mypage/info";
    }
}
