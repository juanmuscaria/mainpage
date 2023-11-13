package com.juanmuscaria.web.mainpage;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("teapot")
@Getter
public class TeapotConfig {
  private final Map<String, Map<String, ProjectMetadata>> projects = new HashMap<>();

  @Getter
  @Setter
  @ToString
  public static class ProjectMetadata {
    @Nullable
    private String displayName;
  }

  // Lazy way to define empty project metadata
  public static class StringToProjectMetadataConverter implements Converter<String, ProjectMetadata> {
    @Override
    public ProjectMetadata convert(@Nonnull String source) {
      return new ProjectMetadata();
    }
  }
}
