package com.juanmuscaria.web.mainpage.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Controller
@Configuration
public class IndexController implements WebMvcConfigurer {
  @GetMapping("/")
  public String viewIndex(Model model) {
    model.addAttribute("page", "crt");
    return "redirect:" + MainPageController.MAINPAGE_BASE_PATH;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/boldoTest").setViewName("boldo");
  }
}
