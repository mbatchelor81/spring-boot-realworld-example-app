package com.ftgo.error;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FtgoErrorHandlingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public GlobalExceptionHandler globalExceptionHandler(Tracer tracer) {
    return new GlobalExceptionHandler(tracer);
  }
}
