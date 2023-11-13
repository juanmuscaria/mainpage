package com.juanmuscaria.web.mainpage.controller;

import com.juanmuscaria.web.mainpage.model.ProjectInfo;
import com.juanmuscaria.web.mainpage.service.TeapotProjectProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Random;

@Controller
@Configuration
@RequestMapping(TeapotController.BASE_PATH)
public class TeapotController implements WebMvcConfigurer {
  private final Random random = new Random("teapot".hashCode());
  static final String BASE_PATH = "/teapot";
  private Resource[] cachedSvgs;

  @Autowired
  private TeapotProjectProvider provider;
  @Autowired
  private ResourcePatternResolver resourceLoader;

  @ModelAttribute("baseUrl")
  public String baseUrl() {
    return BASE_PATH;
  }

  //@SneakyThrows(InterruptedException.class)
  @ModelAttribute("projects")
  public Collection<ProjectInfo> projects() {
    //provider.awaitReady();
    return provider.getProjects();
  }

  @ModelAttribute("teapot")
  public TeapotController teapot() {
    return this;
  }

  @GetMapping
  public String index(Model model, HttpServletResponse response) {
    response.setStatus(418);
    return "teapot/index";
  }

  @SuppressWarnings("unused") // used in templates
  public String readResource(String resourcePath) {
    try {
      Resource resource = resourceLoader.getResource("classpath:" + resourcePath);

      if (resource.exists()) {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      } else {
        return null;
      }
    } catch (IOException e) {
      return null;
    }
  }

  @SuppressWarnings("unused") // used in templates
  public String randomSvgIcon() {
    if (cachedSvgs == null) {
      try {
        cachedSvgs = resourceLoader.getResources("classpath:/static/material-icons@svg/**/*.svg");
        if (cachedSvgs.length == 0) {
          cachedSvgs = null;
          throw new RuntimeException("aaaaa");
        }
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    try {
      //lol
      return "data:image/svg+xml," + URLEncoder.encode(cachedSvgs[random.nextInt(cachedSvgs.length)]
        .getContentAsString(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    var lci = new LocaleChangeInterceptor();
    registry.addInterceptor(lci);
  }
}
