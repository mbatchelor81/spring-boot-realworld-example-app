package com.ftgo.logging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public CorrelationIdFilter correlationIdFilter(
      @Value("${spring.application.name:unknown}") String serviceName) {
    return new CorrelationIdFilter(serviceName);
  }

  @Bean
  @ConditionalOnMissingBean(name = "correlationIdFilterRegistration")
  public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration(
      CorrelationIdFilter filter) {
    FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(filter);
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    return registration;
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
  public LoggingAspect loggingAspect() {
    return new LoggingAspect();
  }
}
