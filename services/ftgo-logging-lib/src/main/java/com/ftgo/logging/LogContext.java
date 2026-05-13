package com.ftgo.logging;

import org.slf4j.MDC;

/** Utility for managing MDC fields in a consistent way across all FTGO services. */
public final class LogContext {

  public static final String USER_ID = "userId";
  public static final String REQUEST_ID = "requestId";
  public static final String TRACE_ID = "traceId";
  public static final String SPAN_ID = "spanId";
  public static final String SERVICE_NAME = "serviceName";
  public static final String CORRELATION_ID = "correlationId";

  private LogContext() {}

  public static void setUserId(String userId) {
    MDC.put(USER_ID, userId);
  }

  public static void setRequestId(String requestId) {
    MDC.put(REQUEST_ID, requestId);
  }

  public static void setTraceId(String traceId) {
    MDC.put(TRACE_ID, traceId);
  }

  public static void setSpanId(String spanId) {
    MDC.put(SPAN_ID, spanId);
  }

  public static void setServiceName(String serviceName) {
    MDC.put(SERVICE_NAME, serviceName);
  }

  public static void setCorrelationId(String correlationId) {
    MDC.put(CORRELATION_ID, correlationId);
  }

  public static void put(String key, String value) {
    MDC.put(key, value);
  }

  public static String get(String key) {
    return MDC.get(key);
  }

  public static void remove(String key) {
    MDC.remove(key);
  }

  public static void clear() {
    MDC.clear();
  }
}
