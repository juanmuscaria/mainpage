package com.juanmuscaria.web.mainpage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
    http
      .headers((headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)))
      .authorizeHttpRequests((requests) -> requests
        .anyRequest().permitAll()
      );
//      .formLogin((form) -> form
//        .loginPage("/login")
//        .permitAll()
//      )
//      .logout(LogoutConfigurer::permitAll);
    return http.build();
  }

//  @Bean
//  public UserDetailsService userDetailsService() {
//    UserDetails user =
//      User.withDefaultPasswordEncoder()
//        .username("user")
//        .password("password")
//        .roles("USER")
//        .build();
//
//    return new InMemoryUserDetailsManager(user);
//  }
}