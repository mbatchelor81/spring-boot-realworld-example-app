package com.ftgo.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

  @Bean
  public ReactiveResilience4JCircuitBreakerFactory reactiveCircuitBreakerFactory(
      CircuitBreakerRegistry circuitBreakerRegistry, TimeLimiterRegistry timeLimiterRegistry) {
    ReactiveResilience4JCircuitBreakerFactory factory =
        new ReactiveResilience4JCircuitBreakerFactory(circuitBreakerRegistry, timeLimiterRegistry);
    return factory;
  }

  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
    return factory ->
        factory.configureDefault(
            id ->
                new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(
                        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .failureRateThreshold(50)
                            .waitDurationInOpenState(java.time.Duration.ofSeconds(30))
                            .permittedNumberOfCallsInHalfOpenState(5)
                            .slowCallRateThreshold(80)
                            .slowCallDurationThreshold(java.time.Duration.ofSeconds(5))
                            .build())
                    .timeLimiterConfig(
                        io.github.resilience4j.timelimiter.TimeLimiterConfig.custom()
                            .timeoutDuration(java.time.Duration.ofSeconds(10))
                            .build())
                    .build());
  }
}
