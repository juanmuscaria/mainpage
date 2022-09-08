package com.juanmuscaria.web.mainpage.controller;

import com.juanmuscaria.web.mainpage.AppConfig;
import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import com.juanmuscaria.web.mainpage.service.GitProjectFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.ArrayList;
import java.util.Map;

@Controller
@Configuration
public class MainPageController implements WebMvcConfigurer {

    @Autowired
    private GitProjectFetcher fetcher;
    @Autowired
    private AppConfig config;

    @GetMapping("/")
    public String viewAbout(Model model) {
        model.addAttribute("page", "about");
        return "about";
    }

    @GetMapping("/projects")
    public String viewProjects(Model model) {
        model.addAttribute("page", "projects");
        var projects = new ArrayList<ProjectInfo>(config.getProjects().size());
        for (Map.Entry<String, String> entry : config.getProjects().entrySet()) {
            projects.add(fetcher.fetch(entry.getValue(), entry.getKey()));
        }
        model.addAttribute("projects", projects);
        return "projects";
    }

    @GetMapping("/contact")
    public String viewContact(Model model) {
        model.addAttribute("page", "contact");
        return "contact";
    }

    @GetMapping("/duck")
    public String duck(Model model) {
        model.addAttribute("page", "duck");
        return "duck";
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
