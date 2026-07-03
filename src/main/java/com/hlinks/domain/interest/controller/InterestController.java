package com.hlinks.domain.interest.controller;

import com.hlinks.domain.interest.dto.InterestDto;
import com.hlinks.domain.interest.dto.InterestSubmitRequest;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
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

        setupFormModel(
                model,
                null,
                "/interests/setup",
                "첫 설정",
                "관심분야를 선택해주세요",
                "선택한 관심분야는 앞으로 교육 콘텐츠, 커리어 추천, IT 뉴스 추천에 활용됩니다. 최대 5개까지 선택할 수 있어요.",
                "관심분야 저장하기",
                "관심분야는 최소 1개 이상 선택해야 합니다. 나중에 마이페이지에서 수정할 수 있도록 확장할 예정입니다."
        );

        return "interest/setup";
    }

    // 선택한 관심분야(스킬) 저장하고 홈으로 되돌려보냄
    @PostMapping("/interests/setup")
    public String setup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            InterestSubmitRequest request,
            Model model
    ) {
        interestService.saveUserInterests(userDetails.getUserId(), request.getSkillIds());

        return "redirect:/";
    }

    @GetMapping("/interests/edit")
    public String editForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        List<Long> selectedSkillIds = interestService.getUserInterests(userDetails.getUserId()).stream()
                .map(InterestDto::getSkillId)
                .toList();

        setupFormModel(
                model,
                selectedSkillIds,
                "/interests/edit",
                "관심분야 수정",
                "관심분야를 수정해주세요",
                "수정한 관심분야는 앞으로 교육 콘텐츠, 커리어 추천, IT 뉴스 추천에 반영됩니다. 최대 5개까지 선택할 수 있어요.",
                "관심분야 수정하기",
                "관심분야는 최소 1개 이상 선택해야 합니다."
        );

        return "interest/setup";
    }

    @PostMapping("/interests/edit")
    public String edit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            InterestSubmitRequest request
    ) {
        interestService.saveUserInterests(userDetails.getUserId(), request.getSkillIds());

        return "redirect:/mypage";
    }

    @ExceptionHandler(BaseException.class)
    public String handleBaseException(BaseException e, HttpServletRequest request, Model model) {
        log.warn("Interest setup error: {}", e.getMessage());

        return setupFormWithError(model, extractSelectedSkillIds(request), e.getMessage(), true, isEditRequest(request));
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, Model model) {
        log.error("Unexpected interest setup error", e);

        return setupFormWithError(
                model,
                extractSelectedSkillIds(request),
                "관심분야 저장 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.",
                true,
                isEditRequest(request)
        );
    }

    private String setupFormWithError(
            Model model,
            List<Long> selectedSkillIds,
            String errorMessage,
            boolean errorModalOpen,
            boolean editMode
    ) {
        if (editMode) {
            setupFormModel(
                    model,
                    selectedSkillIds,
                    "/interests/edit",
                    "관심분야 수정",
                    "관심분야를 수정해주세요",
                    "수정한 관심분야는 앞으로 교육 콘텐츠, 커리어 추천, IT 뉴스 추천에 반영됩니다. 최대 5개까지 선택할 수 있어요.",
                    "관심분야 수정하기",
                    "관심분야는 최소 1개 이상 선택해야 합니다."
            );
        } else {
            setupFormModel(
                    model,
                    selectedSkillIds,
                    "/interests/setup",
                    "첫 설정",
                    "관심분야를 선택해주세요",
                    "선택한 관심분야는 앞으로 교육 콘텐츠, 커리어 추천, IT 뉴스 추천에 활용됩니다. 최대 5개까지 선택할 수 있어요.",
                    "관심분야 저장하기",
                    "관심분야는 최소 1개 이상 선택해야 합니다. 나중에 마이페이지에서 수정할 수 있도록 확장할 예정입니다."
            );
        }

        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("errorModalOpen", errorModalOpen);

        return "interest/setup";
    }

    private void setupFormModel(
            Model model,
            List<Long> selectedSkillIds,
            String formAction,
            String modalEyebrow,
            String modalTitle,
            String modalDescription,
            String submitButtonText,
            String footerText
    ) {
        model.addAttribute("interests", getAllInterestsSafely());
        model.addAttribute("request", new InterestSubmitRequest());
        model.addAttribute("selectedSkillIds", selectedSkillIds);
        model.addAttribute("formAction", formAction);
        model.addAttribute("modalEyebrow", modalEyebrow);
        model.addAttribute("modalTitle", modalTitle);
        model.addAttribute("modalDescription", modalDescription);
        model.addAttribute("submitButtonText", submitButtonText);
        model.addAttribute("footerText", footerText);
    }

    private boolean isEditRequest(HttpServletRequest request) {
        return request.getRequestURI().contains("/interests/edit");
    }

    private List<Long> extractSelectedSkillIds(HttpServletRequest request) {
        String[] skillIds = request.getParameterValues("skillIds");

        if (skillIds == null) {
            return null;
        }

        return Arrays.stream(skillIds)
                .map(Long::valueOf)
                .toList();
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
