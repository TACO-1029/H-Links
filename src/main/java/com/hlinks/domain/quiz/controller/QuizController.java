package com.hlinks.domain.quiz.controller;

import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizGenerateRequest;
import com.hlinks.domain.quiz.ai.service.AiQuizService;
import com.hlinks.domain.quiz.service.QuizGenerateService;
import com.hlinks.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final AiQuizService aiQuizService;
    private final QuizGenerateService quizGenerateService;

    @PostMapping("/generate")
    public SuccessResponse<List<QuizCreateRequest>> generate(@RequestBody QuizGenerateRequest request) {
        return SuccessResponse.from(aiQuizService.generateQuizzes(request));
    }

    @PostMapping("/chapters/{chapterId}/generate")
    public SuccessResponse<List<QuizCreateRequest>> generateAndSaveByChapter(
            @PathVariable Long chapterId,
            @RequestParam(defaultValue = "3") int quizCount,
            @RequestParam(defaultValue = "MEDIUM") String difficulty
    ) {
        return SuccessResponse.from(
                quizGenerateService.generateAndSaveQuizzes(chapterId, quizCount, difficulty)
        );
    }
}
