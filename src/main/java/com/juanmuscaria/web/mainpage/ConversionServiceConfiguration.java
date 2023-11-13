package com.juanmuscaria.web.mainpage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@Order(Integer.MIN_VALUE + 1_000) // Run before other configurations!
public class ConversionServiceConfiguration {
  @Autowired
  private ConfigurableEnvironment environment;

  // System-wide custom converters.
  @PostConstruct
  public void addCustomConverters() {
    var conversionService = environment.getConversionService();
    conversionService.addConverter(new TeapotConfig.StringToProjectMetadataConverter());
  }
}
