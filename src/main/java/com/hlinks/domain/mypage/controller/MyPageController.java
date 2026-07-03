package com.hlinks.domain.mypage.controller;

import com.hlinks.domain.interest.dto.InterestDto;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.domain.recommend.kcy.service.KcyService;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MyPageController {

    private final KcyService kcyService;
    private final InterestService interestService;

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
}
