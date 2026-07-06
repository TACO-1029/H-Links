package com.hlinks.domain.recommend.kcy.controller;

import com.hlinks.domain.recommend.kcy.dto.KcyScoreDto;
import com.hlinks.domain.recommend.kcy.dto.KcySubmitRequest;
import com.hlinks.domain.recommend.kcy.service.KcyService;
import com.hlinks.domain.recommend.kcy.type.KcyType;
import com.hlinks.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
/*
@SessionAttributes("kcyResult")
- 모델 데이터를 '아주 잠깐!' 세션에 보관하는 기능. kcyResult라는 모델 데이터를 여러 요청 사이에서 유지하고 싶을 때 사용
- 없어도 되긴 합니다. FlashAttribute 자체가 리다이렉트 시 데이터를 한번 넘겨주기 때문에..
*/
public class KcyController {

    private final KcyService kcyService;

    // 테스트 화면에 Kcy결과 담아서 제출할 거 담아서 보냅니다.
    @GetMapping("/kcy")
    public String test(Model model) {
        model.addAttribute("activeMenu", "kcy");
        model.addAttribute("activeSubMenu", "kcy");
        model.addAttribute("questions", kcyService.getQuestions());
        model.addAttribute("kcySubmitRequest", new KcySubmitRequest());

        return "recommend/kcy/test";
    }

    // 사용자가 고른 답변을 제출합니다. 앞에서 넘겨준 KcySubmitRequest 객체가 여기로 다시 들어옵니다.
    @PostMapping("/kcy/adaptive-flow")
    @org.springframework.web.bind.annotation.ResponseBody
    public com.hlinks.domain.recommend.kcy.dto.KcyAdaptiveResponse getNextAdaptiveQuestion(
            @org.springframework.web.bind.annotation.RequestBody com.hlinks.domain.recommend.kcy.dto.KcyAdaptiveRequest request) {
        return kcyService.getNextAdaptiveQuestion(request);
    }

    @PostMapping("/kcy")
    public String submit(
            com.hlinks.domain.recommend.kcy.dto.KcySubmitRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        KcyScoreDto score = kcyService.submit(
                userDetails.getUserId(),
                request
        );
        KcyType type = score.toKcyType();

        // PRG 패턴이라고 합니다. 새로고침 했을 때, POST가 반복 실행되는 것을 막아줍니다.
        redirectAttributes.addFlashAttribute("kcyType", type);
        redirectAttributes.addFlashAttribute("kcyScore", score);

        return "redirect:/kcy/result";
    }

    @GetMapping("/kcy/result")
    public String result(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        model.addAttribute("activeMenu", "kcy");
        model.addAttribute("activeSubMenu", "kcy");
        model.addAttribute("userName", userDetails.getName());

        if (!model.containsAttribute("kcyType") || !model.containsAttribute("kcyScore")) {
            return "redirect:/kcy";
        }

        return "recommend/kcy/result";
    }
}
