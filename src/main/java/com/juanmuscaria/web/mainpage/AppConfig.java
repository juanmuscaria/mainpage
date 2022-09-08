package com.juanmuscaria.web.mainpage;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Getter
public class AppConfig {
    private final Map<String, String> projects = new LinkedHashMap<>();
}
