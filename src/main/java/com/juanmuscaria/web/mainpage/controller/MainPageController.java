package com.juanmuscaria.web.mainpage.controller;

import com.juanmuscaria.web.mainpage.AppConfig;
import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import com.juanmuscaria.web.mainpage.service.GitProjectFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

@Controller
@Configuration
@RequestMapping(MainPageController.BASE_PATH)
public class MainPageController implements WebMvcConfigurer {
  static final String BASE_PATH = "/main";

  @Autowired
  private GitProjectFetcher fetcher;
  @Autowired
  private AppConfig config;

  @ModelAttribute("baseUrl")
  public String baseUrl() {
    return BASE_PATH;
  }

  @GetMapping
  public String viewAbout(Model model) {
    model.addAttribute("page", "about");
    return "mainpage/about";
  }

  @GetMapping("/projects")
  public String viewProjects(Model model) {
    model.addAttribute("page", "projects");
    var projects = new ArrayList<ProjectInfo>(config.getProjects().size());
    for (Map.Entry<String, String> entry : config.getProjects().entrySet()) {
      projects.add(fetcher.fetch(entry.getValue(), entry.getKey()));
    }
    projects.sort(Collections.reverseOrder(Comparator.comparingInt(ProjectInfo::stars)));
    model.addAttribute("projects", projects);
    return "mainpage/projects";
  }

  @GetMapping("/contact")
  public String viewContact(Model model) {
    model.addAttribute("page", "contact");
    return "mainpage/contact";
  }

  @GetMapping("/duck")
  public String duck(Model model) {
    model.addAttribute("page", "duck");
    return "mainpage/duck";
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    var lci = new LocaleChangeInterceptor();
    registry.addInterceptor(lci);
  }
}
