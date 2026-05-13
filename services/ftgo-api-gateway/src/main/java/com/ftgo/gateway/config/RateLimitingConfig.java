package com.ftgo.gateway.config;

import java.security.Principal;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingConfig {

  @Bean
  public KeyResolver apiKeyResolver() {
    return exchange ->
        exchange
            .getPrincipal()
            .map(Principal::getName)
            .defaultIfEmpty(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "anonymous");
  }
}
