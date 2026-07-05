package com.hlinks.domain.quiz.controller;

import com.hlinks.domain.quiz.dto.ChapterQuizPageResponse;
import com.hlinks.domain.quiz.dto.ChapterQuizResultResponse;
import com.hlinks.domain.quiz.service.QuizAttemptService;
import com.hlinks.global.exception.BaseException;
import com.hlinks.global.response.code.ErrorResponseCode;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/courses/{courseId}/chapters/{chapterId}/quiz")
@RequiredArgsConstructor
public class QuizAttemptController {

    private static final String ANSWER_PREFIX = "answers[";

    private final QuizAttemptService quizAttemptService;

    @GetMapping
    public String quizPage(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        ChapterQuizPageResponse quizPage =
                quizAttemptService.getChapterQuizPage(courseId, chapterId, userDetails.getUserId());
        model.addAttribute("quizPage", quizPage);
        model.addAttribute("activeMenu", "courses");
        return "quiz/chapter-quiz";
    }

    @PostMapping
    public String submitQuiz(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @RequestParam Map<String, String> requestParams,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long attemptId = quizAttemptService.submitChapterQuiz(
                courseId,
                chapterId,
                userDetails.getUserId(),
                extractAnswers(requestParams)
        );

        return "redirect:/courses/" + courseId + "/chapters/" + chapterId + "/quiz/result/" + attemptId;
    }

    @GetMapping("/result/{attemptId}")
    public String resultPage(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @PathVariable Long attemptId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        ChapterQuizResultResponse result =
                quizAttemptService.getAttemptResult(attemptId, userDetails.getUserId());
        if (!courseId.equals(result.getCourseId()) || !chapterId.equals(result.getChapterId())) {
            throw new BaseException(ErrorResponseCode.INVALID_REQUEST_PARAMETER, "퀴즈 결과 경로가 올바르지 않습니다.");
        }
        model.addAttribute("result", result);
        model.addAttribute("activeMenu", "courses");
        return "quiz/chapter-quiz-result";
    }

    private Map<Long, Long> extractAnswers(Map<String, String> requestParams) {
        Map<Long, Long> answers = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith(ANSWER_PREFIX) || !key.endsWith("]")) {
                continue;
            }

            try {
                Long quizId = Long.valueOf(key.substring(ANSWER_PREFIX.length(), key.length() - 1));
                Long optionId = Long.valueOf(entry.getValue());
                answers.put(quizId, optionId);
            } catch (NumberFormatException e) {
                throw new BaseException(ErrorResponseCode.BAD_REQUEST, "제출한 답안 형식이 올바르지 않습니다.");
            }
        }

        return answers;
    }
}
