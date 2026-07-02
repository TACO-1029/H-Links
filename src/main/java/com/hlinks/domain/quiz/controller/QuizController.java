package com.hlinks.domain.quiz.controller;

import com.hlinks.domain.quiz.dto.QuizCreateRequest;
import com.hlinks.domain.quiz.dto.QuizGenerateRequest;
import com.hlinks.domain.quiz.service.AiQuizService;
import com.hlinks.global.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final AiQuizService aiQuizService;

    @PostMapping("/generate")
    public SuccessResponse<List<QuizCreateRequest>> generate(@RequestBody QuizGenerateRequest request) {
        return SuccessResponse.from(aiQuizService.generateQuizzes(request));
    }
}
