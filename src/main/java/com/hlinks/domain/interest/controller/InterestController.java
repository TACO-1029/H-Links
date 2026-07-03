package com.hlinks.domain.interest.controller;

import com.hlinks.domain.interest.dto.InterestSubmitRequest;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @GetMapping("/interests/setup")
    public String setupForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        // 관심분야가 있다면 홈으로 보냅니다.
        if (interestService.hasInterests(userDetails.getUserId())) {
            return "redirect:/";
        }

        model.addAttribute("interests", interestService.getAllInterests());
        model.addAttribute("request", new InterestSubmitRequest());

        return "interest/setup";
    }
    // 선택한 관심분야(스킬) 저장하고 홈으로 되돌려보냄
    @PostMapping("/interests/setup")
    public String setup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            InterestSubmitRequest request
    ) {
        interestService.saveUserInterests(userDetails.getUserId(), request.getSkillIds());

        return "redirect:/";
    }
}