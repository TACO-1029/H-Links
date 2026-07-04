package com.hlinks.domain.career.controller;

import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.service.CareerService;
import com.hlinks.domain.interest.service.InterestService;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/courses/career-path")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final InterestService interestService;

    @GetMapping
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("activeSubMenu", "careerPath");

        if (userDetails != null && careerService.hasDiagnosis(userDetails.getUserId())) {
            // 진단 이력이 존재할 경우 대시보드로 이동
            return "redirect:/courses/career-path/dashboard";
        }
        // 최초 접속일 경우 welcome 페이지 노출
        return "career/welcome";
    }

    @GetMapping("/start")
    public String startDiagnosis(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long diagnosisId = careerService.createDiagnosis(userDetails.getUserId());
        return "redirect:/courses/career-path/survey?diagnosisId=" + diagnosisId;
    }

    @GetMapping("/survey")
    public String surveyForm(
            @RequestParam("diagnosisId") Long diagnosisId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("activeSubMenu", "careerPath");
        model.addAttribute("skills", interestService.getAllInterests());
        model.addAttribute("diagnosisId", diagnosisId);
        return "career/survey";
    }

    @PostMapping("/survey")
    public String submitSurvey(
            @RequestParam("diagnosisId") Long diagnosisId,
            @RequestParam(value = "skillIds", required = false) List<Long> skillIds,
            @RequestParam(value = "difficulty", defaultValue = "중") String difficulty,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        if (skillIds == null || skillIds.isEmpty()) {
            model.addAttribute("activeMenu", "courses");
            model.addAttribute("activeSubMenu", "careerPath");
            model.addAttribute("skills", interestService.getAllInterests());
            model.addAttribute("diagnosisId", diagnosisId);
            model.addAttribute("errorMessage", "목표 스킬을 최소 1개 이상 선택해 주세요.");
            return "career/survey";
        }

        careerService.saveTargetSkills(diagnosisId, skillIds);
        log.info("커리어 진단 목표 스킬 저장 완료 - UserId: {}, DiagnosisId: {}, 난이도: {}", userDetails.getUserId(), diagnosisId, difficulty);

        return "redirect:/courses/career-path/level-test-pending?diagnosisId=" + diagnosisId + "&difficulty=" + difficulty;
    }

    @GetMapping("/level-test-pending")
    public String levelTestPending(
            @RequestParam("diagnosisId") Long diagnosisId,
            @RequestParam(value = "difficulty", defaultValue = "중") String difficulty,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("activeSubMenu", "careerPath");
        model.addAttribute("diagnosisId", diagnosisId);
        model.addAttribute("difficulty", difficulty);

        // UI에 선택된 분야(첫번째 스킬의 분류 등)를 노출하기 위해 가상의 분석 필드 제공
        model.addAttribute("mode", "표준 진단");
        return "career/level_test_pending";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("activeSubMenu", "careerPath");
        model.addAttribute("userName", userDetails.getName());

        Long userId = userDetails.getUserId();
        String status = "WELCOME";
        CareerDiagnosis latestDiagnosis = null;

        try {
            if (careerService.hasDiagnosis(userId)) {
                latestDiagnosis = careerService.findLatestDiagnosis(userId);
                Long diagId = latestDiagnosis.getDiagnosisId();
                String buildStatus = latestDiagnosis.getLevelTestBuildStatus();

                int targetSkillCount = careerService.getTargetSkillCount(diagId);
                int questionCount = careerService.getQuestionCount(diagId);
                int answerCount = careerService.getAnswerCount(diagId);
                int recCount = careerService.getRecommendationCount(diagId);

                if (targetSkillCount == 0) {
                    status = "WELCOME"; // 목표 기술 선택중
                } else if ("PENDING".equals(buildStatus) || "PROCESSING".equals(buildStatus)) {
                    status = "CREATING"; // AI 레벨테스트 생성중
                } else if ("FAILED".equals(buildStatus)) {
                    status = "FAILED";
                } else if ("COMPLETED".equals(buildStatus)) {
                    if (recCount > 0 || (questionCount > 0 && answerCount == questionCount)) {
                        status = "COMPLETED"; // 진단 전체 완료 (다시 진단)
                    } else if (answerCount > 0 && answerCount < questionCount) {
                        status = "RESUMING"; // 레벨 테스트 이어하기
                    } else {
                        status = "READY"; // 레벨 테스트 시작 가능
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load career dashboard status for user: {}", userId, e);
        }

        model.addAttribute("status", status);
        model.addAttribute("diagnosis", latestDiagnosis);
        return "career/dashboard";
    }
}
