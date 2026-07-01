package com.hlinks.domain.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TeamController {

    @GetMapping("/team")
    public String index(Model model) {
        model.addAttribute("activeMenu", "team");
        return "team/index";
    }
}
