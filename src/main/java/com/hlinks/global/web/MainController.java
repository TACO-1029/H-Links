package com.hlinks.global.web;

import com.hlinks.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping({"/", "/home"})
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("activeMenu", "home");
        model.addAttribute("userName", userDetails != null ? userDetails.getName() : null);

        return "home/index";
    }

    @GetMapping("/recommend")
    public String recommend(Model model) {
        model.addAttribute("activeMenu", "recommend");

        return "recommend/index";
    }
}
