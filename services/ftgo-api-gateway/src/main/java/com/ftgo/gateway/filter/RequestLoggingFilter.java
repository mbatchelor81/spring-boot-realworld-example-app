package com.ftgo.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

  private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
  private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
    String method = request.getMethodValue();
    String path = request.getURI().getPath();
    String clientIp =
        request.getRemoteAddress() != null
            ? request.getRemoteAddress().getAddress().getHostAddress()
            : "unknown";

    long startTime = System.currentTimeMillis();

    log.info(
        "Incoming request: method={} path={} clientIp={} correlationId={}",
        method,
        path,
        clientIp,
        correlationId);

    return chain
        .filter(exchange)
        .then(
            Mono.fromRunnable(
                () -> {
                  ServerHttpResponse response = exchange.getResponse();
                  long duration = System.currentTimeMillis() - startTime;
                  log.info(
                      "Outgoing response: method={} path={} status={} duration={}ms"
                          + " correlationId={}",
                      method,
                      path,
                      response.getStatusCode(),
                      duration,
                      correlationId);
                }));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
