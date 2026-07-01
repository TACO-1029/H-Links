package com.hlinks.domain.hr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HrController {
  @GetMapping("/hr")
  public String index() {
    return "hr/index";
  }
}
