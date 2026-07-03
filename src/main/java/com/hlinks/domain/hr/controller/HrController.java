package com.hlinks.domain.hr.controller;

import com.hlinks.domain.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HrController {

    private final QuizService quizService;

    @GetMapping("/hr")
    public String index(Model model) {
        model.addAttribute("activeMenu", "hr");
        return "hr/index";
    }

    @GetMapping("/hr/quizzes/ai")
    public String aiGeneratedQuizzes(Model model) {
        model.addAttribute("activeMenu", "hr");
        model.addAttribute("quizzes", quizService.getAiGeneratedQuizzes());
        return "hr/quizzes-ai";
    }
}
