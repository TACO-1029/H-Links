package com.hlinks.domain.career.controller;

import com.hlinks.domain.career.entity.CareerDiagnosis;
import com.hlinks.domain.career.service.CareerService;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationRequest;
import com.hlinks.domain.recommend.course.dto.CourseRecommendationResponse;
import com.hlinks.domain.recommend.course.dto.LevelTestSkillResultRequest;
import com.hlinks.domain.recommend.course.dto.RecommendedCourseDto;
import com.hlinks.domain.recommend.course.service.CourseRecommendationService;
import com.hlinks.global.security.CustomUserDetails;
import com.hlinks.global.exception.BaseException;
import com.hlinks.domain.career.exception.CareerErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/courses/career-path")
@RequiredArgsConstructor
public class CareerController {

    private static final String ACTIVE_MENU = "recommend";
    private static final String ACTIVE_SUB_MENU = "careerPath";

    private final CareerService careerService;
    private final CourseRecommendationService courseRecommendationService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        setActiveMenu(model);

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
        setActiveMenu(model);
        model.addAttribute("skills", careerService.getAllActiveSkills());
        model.addAttribute("diagnosisId", diagnosisId);
        return "career/survey";
    }

    @PostMapping("/survey")
    public String submitSurvey(
            @RequestParam("diagnosisId") Long diagnosisId,
            @RequestParam(value = "skillIds", required = false) List<Long> skillIds,
            @RequestParam(value = "difficulties", required = false) List<String> difficulties,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (skillIds == null || skillIds.isEmpty()) {
            setActiveMenu(model);
            model.addAttribute("skills", careerService.getAllActiveSkills());
            model.addAttribute("diagnosisId", diagnosisId);
            model.addAttribute("errorMessage", "목표 스킬을 최소 1개 이상 선택해 주세요.");
            return "career/survey";
        }

        careerService.saveTargetSkills(diagnosisId, skillIds, difficulties);
        log.info("커리어 진단 목표 스킬 저장 완료 - UserId: {}, DiagnosisId: {}, 난이도: {}", userDetails.getUserId(), diagnosisId, difficulties);
        careerService.buildLevelTestAsync(diagnosisId, skillIds, difficulties);
        log.info("커리어 진단 목표 스킬 저장 및 비동기 생성 요청 완료 - UserId: {}, DiagnosisId: {}, Difficulties: {}", userDetails.getUserId(), diagnosisId, difficulties);

        String firstDiff = (difficulties != null && !difficulties.isEmpty()) ? difficulties.get(0) : "중";
        redirectAttributes.addFlashAttribute("diagnosisId", diagnosisId);
        redirectAttributes.addFlashAttribute("difficulty", firstDiff);
        return "redirect:/courses/career-path/level-test-pending";
    }

    @GetMapping("/level-test-pending")
    public String levelTestPending(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        setActiveMenu(model);
        
        Long diagnosisId = (Long) model.asMap().get("diagnosisId");
        String difficulty = (String) model.asMap().get("difficulty");
        
        if (diagnosisId == null) {
            diagnosisId = careerService.findLatestDiagnosisId(userDetails.getUserId());
        }
        if (difficulty == null) {
            difficulty = "중";
        }

        model.addAttribute("diagnosisId", diagnosisId);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("mode", "표준 진단");
        return "career/level_test_pending";
    }

    @org.springframework.web.bind.annotation.ResponseBody
    @GetMapping("/build-status")
    public Map<String, String> getBuildStatus(
            @RequestParam("diagnosisId") Long diagnosisId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CareerDiagnosis diagnosis = careerService.findDiagnosisById(diagnosisId);
        if (diagnosis == null || !diagnosis.getUserId().equals(userDetails.getUserId())) {
            return Map.of("status", "FAILED");
        }
        return Map.of("status", diagnosis.getLevelTestBuildStatus());
    }

    @GetMapping("/level-test")
    public String levelTest(
            @RequestParam(value = "diagnosisId", required = false) Long diagnosisId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        setActiveMenu(model);
        if (diagnosisId == null) {
            diagnosisId = (Long) model.asMap().get("diagnosisId");
        }
        if (diagnosisId == null) {
            diagnosisId = careerService.findLatestDiagnosisId(userDetails.getUserId());
        }
        model.addAttribute("diagnosisId", diagnosisId);
        model.addAttribute("questions", careerService.getLevelTestQuestions(diagnosisId));
        return "career/level_test";
    }

    @PostMapping("/level-test/submit")
    public String submitLevelTest(
            @RequestParam("diagnosisId") Long diagnosisId,
            @RequestParam("questionIds") List<Long> questionIds,
            @RequestParam("selectedOptionIds") List<Long> selectedOptionIds,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        CareerDiagnosis diagnosis = careerService.findDiagnosisById(diagnosisId);
        if (diagnosis == null || !diagnosis.getUserId().equals(userDetails.getUserId())) {
            throw new BaseException(CareerErrorCode.DIAGNOSIS_NOT_FOUND);
        }
        careerService.submitAnswers(diagnosisId, userDetails.getUserId(), questionIds, selectedOptionIds);
        redirectAttributes.addFlashAttribute("diagnosisId", diagnosisId);
        return "redirect:/courses/career-path/result";
    }

    @GetMapping("/result")
    public String viewResult(
            @RequestParam(value = "diagnosisId", required = false) Long requestDiagnosisId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        setActiveMenu(model);
        Long diagnosisId = requestDiagnosisId;
        if (diagnosisId == null) {
            diagnosisId = (Long) model.asMap().get("diagnosisId");
        }
        if (diagnosisId == null) {
            diagnosisId = careerService.findLatestDiagnosisId(userDetails.getUserId());
        }
        CareerDiagnosis diagnosis = careerService.findDiagnosisById(diagnosisId);
        if (diagnosis == null || !diagnosis.getUserId().equals(userDetails.getUserId())) {
            return "redirect:/courses/career-path/dashboard";
        }

        CareerResultViewData resultData = parseCareerResult(diagnosis);

        model.addAttribute("diagnosisId", diagnosisId);
        model.addAttribute("category", resultData.category());
        model.addAttribute("results", resultData.results());
        model.addAttribute("rawJson", resultData.rawJson());
        model.addAttribute("averageScore", resultData.averageScore());
        model.addAttribute("lowestSkill", resultData.lowestSkillName());
        model.addAttribute("userName", userDetails.getName());
        model.addAttribute("aiSummary", resultData.aiSummary());
        model.addAttribute("recommendedCourses", recommendCourses(resultData.category(), resultData.results()).getCourses());
        return "career/result";
    }

    @SuppressWarnings("unchecked")
    private CareerResultViewData parseCareerResult(CareerDiagnosis diagnosis) {
        String rawJson = diagnosis == null ? null : diagnosis.getLlmSummary();
        String category = "IT 기술";
        String aiSummary = null;
        List<Map<String, Object>> resultsList = new ArrayList<>();
        int averageScore = 0;
        String lowestSkillName = "";

        List<com.hlinks.domain.career.dto.CareerSkillDto> allSkills = careerService.getAllActiveSkills();

        try {
            if (rawJson != null && rawJson.trim().startsWith("{")) {
                Map<String, Object> jsonMap = objectMapper.readValue(rawJson, Map.class);
                String tempCategory = (String) jsonMap.get("category");
                String tempAiSummary = (String) jsonMap.get("aiSummary");
                List<Map<String, Object>> rawResults = (List<Map<String, Object>>) jsonMap.get("results");
                List<Map<String, Object>> enrichedResults = new ArrayList<>();

                double scoreSum = 0;
                int scoreCount = 0;
                int lowestScore = Integer.MAX_VALUE;

                if (rawResults != null) {
                    for (Map<String, Object> result : rawResults) {
                        if (result == null || result.get("skillId") == null || result.get("score") == null) {
                            continue;
                        }

                        Map<String, Object> enriched = new HashMap<>(result);
                        Long skillId = ((Number) result.get("skillId")).longValue();
                        String skillName = allSkills.stream()
                                .filter(skill -> skill.getSkillId().equals(skillId))
                                .map(com.hlinks.domain.career.dto.CareerSkillDto::getSkillName)
                                .findFirst()
                                .orElse("세부 기술");

                        enriched.put("skillName", skillName);
                        enrichedResults.add(enriched);

                        int score = ((Number) result.get("score")).intValue();
                        scoreSum += score;
                        scoreCount++;

                        if (score < lowestScore) {
                            lowestScore = score;
                            lowestSkillName = skillName;
                        }
                    }
                }

                category = tempCategory != null ? tempCategory : "IT 기술";
                aiSummary = tempAiSummary;
                resultsList = enrichedResults;

                if (scoreCount > 0) {
                    averageScore = (int) Math.round(scoreSum / scoreCount);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse career scoring JSON. diagnosisId={}",
                    diagnosis == null ? null : diagnosis.getDiagnosisId(), e);
        }

        return new CareerResultViewData(
                category,
                aiSummary,
                resultsList,
                rawJson,
                averageScore,
                lowestSkillName.isEmpty() ? "핵심 기술" : lowestSkillName
        );
    }

    private CourseRecommendationResponse recommendCourses(String category, List<Map<String, Object>> resultsList) {
        if (resultsList == null || resultsList.isEmpty()) {
            return emptyCourseRecommendation(category);
        }

        try {
            CourseRecommendationRequest request = new CourseRecommendationRequest();
            request.setCategory(category);
            request.setLimit(4);
            request.setResults(resultsList.stream()
                    .map(this::toLevelTestSkillResultRequest)
                    .toList());

            return courseRecommendationService.recommendByLevelTest(request);
        } catch (Exception e) {
            log.warn("Failed to load course recommendations from level test result", e);
            return emptyCourseRecommendation(category);
        }
    }

    private CourseRecommendationResponse emptyCourseRecommendation(String category) {
        return CourseRecommendationResponse.builder()
                .category(category)
                .requestedSkillCount(0)
                .courses(List.of())
                .build();
    }

    private LevelTestSkillResultRequest toLevelTestSkillResultRequest(Map<String, Object> result) {
        LevelTestSkillResultRequest request = new LevelTestSkillResultRequest();
        request.setSkillId(((Number) result.get("skillId")).longValue());
        request.setScore(((Number) result.get("score")).intValue());
        request.setSelectedDifficulty(String.valueOf(result.get("selectedDifficulty")));
        return request;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        setActiveMenu(model);
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
        if (latestDiagnosis != null) {
            try {
                CareerResultViewData resultData = parseCareerResult(latestDiagnosis);
                List<RecommendedCourseDto> recommendedCourses = recommendCourses(
                        resultData.category(),
                        resultData.results()
                ).getCourses();

                model.addAttribute("recommendedCourses", recommendedCourses);
                model.addAttribute("recommendationCategory", resultData.category());
                model.addAttribute("recommendationLowestSkill", resultData.lowestSkillName());
                model.addAttribute("recommendationDiagnosisId", latestDiagnosis.getDiagnosisId());
            } catch (Exception e) {
                log.warn("Failed to load initial career recommendation courses - userId={}, diagnosisId={}",
                        userId, latestDiagnosis.getDiagnosisId(), e);
                model.addAttribute("recommendedCourses", List.of());
                model.addAttribute("recommendationDiagnosisId", latestDiagnosis.getDiagnosisId());
            }
        }
        return "career/dashboard";
    }

    private record CareerResultViewData(
            String category,
            String aiSummary,
            List<Map<String, Object>> results,
            String rawJson,
            int averageScore,
            String lowestSkillName
    ) {
    }

    private void setActiveMenu(Model model) {
        model.addAttribute("activeMenu", ACTIVE_MENU);
        model.addAttribute("activeSubMenu", ACTIVE_SUB_MENU);
    }
}
