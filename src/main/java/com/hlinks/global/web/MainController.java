package com.hlinks.global.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("activeMenu", "home");

        return "home/index";
    }
}
