package com.juanmuscaria.web.mainpage;

import com.juanmuscaria.web.lang.FileMessageSource;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Getter
public class AppConfig {
  private final Map<String, String> projects = new LinkedHashMap<>();

  @Bean
  public LocaleResolver localeResolver() {
    return new SessionLocaleResolver();
  }

  @Bean
  public BeanPostProcessor processMessageSource() {
    return new BeanPostProcessor() {
      @Override
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ("messageSource".equals(beanName) && bean instanceof HierarchicalMessageSource root) {
          /* Teapot md file source */
          {
            var bundle = new FileMessageSource();
            bundle.setBasename("lang/file");
            bundle.setFileExtensions(".md");
            bundle.setParentMessageSource(root);
            bundle.setFallbackToSystemLocale(false);
            root = bundle;
          }
          /* Teapot message source */
          {
            var bundle = new ReloadableResourceBundleMessageSource();
            bundle.setBasename("lang/teapot-messages");
            bundle.setParentMessageSource(root);
            root = bundle;
          }
          return root;
        }
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
      }
    };
  }


//  @Bean
//  public MessageSource messageSource(MessageSourceProperties properties) {
//    HierarchicalMessageSource root;
//
//    /* Teapot message source */ {
//      var bundle = new ReloadableResourceBundleMessageSource();
//      bundle.setBasename("lang.teapot-messages");
//      root = bundle;
//    }
//
//    /* Spring default message source */ {
//      var bundle = new ReloadableResourceBundleMessageSource();
//      if (StringUtils.hasText(properties.getBasename())) {
//        bundle.setBasenames(StringUtils
//          .commaDelimitedListToStringArray(StringUtils.trimAllWhitespace(properties.getBasename())));
//      }
//      if (properties.getEncoding() != null) {
//        bundle.setDefaultEncoding(properties.getEncoding().name());
//      }
//      bundle.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
//      Duration cacheDuration = properties.getCacheDuration();
//      if (cacheDuration != null) {
//        bundle.setCacheMillis(cacheDuration.toMillis());
//      }
//      bundle.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
//      root.setParentMessageSource(bundle);
//      root = bundle;
//    }
//    return root;
//  }
//


//  @Bean
//  @ConfigurationProperties(prefix = "spring.messages")
//  public MessageSourceProperties messageSourceProperties() {
//    return new MessageSourceProperties();
//  }
}
