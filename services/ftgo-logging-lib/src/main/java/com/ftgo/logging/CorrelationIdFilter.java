package com.ftgo.logging;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";
  public static final String SERVICE_NAME_MDC_KEY = "serviceName";
  public static final String REQUEST_ID_MDC_KEY = "requestId";
  public static final String USER_ID_MDC_KEY = "userId";

  private final String serviceName;

  public CorrelationIdFilter(String serviceName) {
    this.serviceName = serviceName;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String correlationId = sanitizeHeaderValue(request.getHeader(CORRELATION_ID_HEADER));
      if (correlationId == null || correlationId.isBlank()) {
        correlationId = UUID.randomUUID().toString();
      }
      String requestId = UUID.randomUUID().toString();
      MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
      MDC.put(SERVICE_NAME_MDC_KEY, serviceName);
      MDC.put(REQUEST_ID_MDC_KEY, requestId);
      String userId = sanitizeHeaderValue(request.getHeader("X-User-ID"));
      if (userId != null && !userId.isBlank()) {
        MDC.put(USER_ID_MDC_KEY, userId);
      }
      response.setHeader(CORRELATION_ID_HEADER, correlationId);
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(CORRELATION_ID_MDC_KEY);
      MDC.remove(SERVICE_NAME_MDC_KEY);
      MDC.remove(REQUEST_ID_MDC_KEY);
      MDC.remove(USER_ID_MDC_KEY);
    }
  }

  private static final int MAX_HEADER_LENGTH = 128;

  static String sanitizeHeaderValue(String value) {
    if (value == null) {
      return null;
    }
    String sanitized = value.replaceAll("[\\r\\n]", "");
    if (sanitized.length() > MAX_HEADER_LENGTH) {
      sanitized = sanitized.substring(0, MAX_HEADER_LENGTH);
    }
    return sanitized;
  }
}
