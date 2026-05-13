package com.ftgo.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingConfig {

  @Bean
  public KeyResolver apiKeyResolver() {
    return exchange -> {
      String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
      if (apiKey != null && !apiKey.isEmpty()) {
        return Mono.just(apiKey);
      }
      // Fall back to client IP for anonymous requests
      String clientIp =
          exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
      if (clientIp != null && !clientIp.isEmpty()) {
        return Mono.just(clientIp.split(",")[0].trim());
      }
      return Mono.just(
          exchange.getRequest().getRemoteAddress() != null
              ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
              : "anonymous");
    };
  }
}
