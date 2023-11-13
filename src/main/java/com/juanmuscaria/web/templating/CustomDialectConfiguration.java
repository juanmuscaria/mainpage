package com.juanmuscaria.web.templating;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.Set;

@Configuration
public class CustomDialectConfiguration {

  @Bean
  public IProcessorDialect customDialect() {
    return new AbstractProcessorDialect("Custom Dialect", "juanmuscaria", 1000) {
      @Override
      public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(new MarkdownAttributeProcessor(dialectPrefix));
      }
    };
  }
}

