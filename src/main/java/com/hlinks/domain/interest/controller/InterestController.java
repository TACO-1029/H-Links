package com.hlinks.domain.interest.controller;

import com.hlinks.domain.interest.dto.InterestSubmitRequest;
import com.hlinks.domain.interest.exception.InterestErrorCode;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
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
            InterestSubmitRequest request,
            Model model
    ) {
        List<Long> skillIds = request.getSkillIds();

        if (skillIds == null || skillIds.isEmpty()) {
            return setupFormWithError(model, skillIds, InterestErrorCode.INTEREST_REQUIRED.getMessage(), true);
        }

        if (skillIds.size() > 5) {
            return setupFormWithError(model, skillIds, InterestErrorCode.INTEREST_TOO_MANY_SELECTED.getMessage(), true);
        }

        interestService.saveUserInterests(userDetails.getUserId(), request.getSkillIds());

        return "redirect:/";
    }

    @ExceptionHandler(BaseException.class)
    public String handleBaseException(BaseException e, Model model) {
        log.warn("Interest setup error: {}", e.getMessage());

        return setupFormWithError(model, null, e.getMessage(), true);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected interest setup error", e);

        return setupFormWithError(model, null, "관심분야 저장 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", true);
    }

    private String setupFormWithError(Model model, List<Long> selectedSkillIds, String errorMessage, boolean errorModalOpen) {
        model.addAttribute("interests", getAllInterestsSafely());
        model.addAttribute("request", new InterestSubmitRequest());
        model.addAttribute("selectedSkillIds", selectedSkillIds);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorModalOpen", errorModalOpen);

        return "interest/setup";
    }

    private List<?> getAllInterestsSafely() {
        try {
            return interestService.getAllInterests();
        } catch (Exception e) {
            log.warn("Failed to load interests for setup error view", e);
            return List.of();
        }
    }
}
