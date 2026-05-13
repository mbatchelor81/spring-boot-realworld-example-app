package com.ftgo.gateway.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Value("${ftgo.gateway.cors.allowed-origins:*}")
  private String allowedOrigins;

  @Value("${ftgo.gateway.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
  private String allowedMethods;

  @Value("${ftgo.gateway.cors.allowed-headers:*}")
  private String allowedHeaders;

  @Value("${ftgo.gateway.cors.allow-credentials:false}")
  private boolean allowCredentials;

  @Value("${ftgo.gateway.cors.max-age:3600}")
  private long maxAge;

  @Bean
  public CorsWebFilter corsWebFilter() {
    CorsConfiguration config = new CorsConfiguration();

    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    boolean hasWildcard = origins.stream().anyMatch(o -> o.contains("*"));
    if (hasWildcard) {
      config.setAllowedOriginPatterns(origins);
    } else {
      config.setAllowedOrigins(origins);
    }

    config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
    config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
    config.setAllowCredentials(allowCredentials);
    config.setMaxAge(maxAge);
    config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Correlation-Id"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsWebFilter(source);
  }
}
