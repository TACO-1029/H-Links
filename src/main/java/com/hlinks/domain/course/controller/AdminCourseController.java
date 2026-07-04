package com.hlinks.domain.course.controller;

import com.hlinks.domain.course.dto.AdminCourseCreateRequest;
import com.hlinks.domain.course.dto.AdminCourseCreateResponse;
import com.hlinks.domain.course.service.AdminCourseService;
import com.hlinks.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class AdminCourseController {

    private final AdminCourseService adminCourseService;

    @GetMapping("/new")
    public String newCourseForm() {
        return "hr/course-new";
    }

    @PostMapping
    public String createCourse(
            @ModelAttribute("courseForm") AdminCourseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser,
            RedirectAttributes redirectAttributes
    ) {
        AdminCourseCreateResponse response = adminCourseService.createCourse(request, loginUser.getUserId());

        redirectAttributes.addFlashAttribute("message", "강의가 등록되었습니다.");
        return "redirect:/courses/" + response.getCourseId();
    }
}
