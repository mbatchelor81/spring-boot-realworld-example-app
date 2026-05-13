package com.ftgo.gateway.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class GatewayFallbackConfig {

  @Bean
  public RouterFunction<ServerResponse> fallbackRoute() {
    return RouterFunctions.route()
        .route(
            request -> request.path().equals("/fallback"),
            request ->
                ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        Map.of(
                            "status", 503,
                            "error", "Service Unavailable",
                            "message",
                                "The downstream service is temporarily unavailable."
                                    + " Please try again later.")))
        .build();
  }
}
