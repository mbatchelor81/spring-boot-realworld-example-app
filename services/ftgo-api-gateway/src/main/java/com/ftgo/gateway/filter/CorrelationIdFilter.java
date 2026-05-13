package com.ftgo.gateway.filter;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
  private static final int MAX_CORRELATION_ID_LENGTH = 64;
  private static final String CORRELATION_ID_PATTERN = "^[a-zA-Z0-9.\\-_]+$";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String correlationId =
        exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

    if (correlationId != null
        && !correlationId.isEmpty()
        && (correlationId.length() > MAX_CORRELATION_ID_LENGTH
            || !correlationId.matches(CORRELATION_ID_PATTERN))) {
      log.warn("Rejected invalid correlation ID: length={}", correlationId.length());
      correlationId = null;
    }

    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = UUID.randomUUID().toString();
    }

    final String finalCorrelationId = correlationId;
    log.debug("Correlation ID: {}", finalCorrelationId);

    ServerHttpRequest mutatedRequest =
        exchange
            .getRequest()
            .mutate()
            .header(CORRELATION_ID_HEADER, finalCorrelationId)
            .build();

    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

    mutatedExchange
        .getResponse()
        .beforeCommit(
            () -> {
              mutatedExchange
                  .getResponse()
                  .getHeaders()
                  .set(CORRELATION_ID_HEADER, finalCorrelationId);
              return Mono.empty();
            });

    return chain.filter(mutatedExchange);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
