package com.ftgo.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class LogContextTest {

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void setsAndGetsUserId() {
    LogContext.setUserId("user-42");
    assertEquals("user-42", MDC.get(LogContext.USER_ID));
  }

  @Test
  void setsAndGetsRequestId() {
    LogContext.setRequestId("req-abc");
    assertEquals("req-abc", MDC.get(LogContext.REQUEST_ID));
  }

  @Test
  void setsAndGetsTraceId() {
    LogContext.setTraceId("trace-123");
    assertEquals("trace-123", MDC.get(LogContext.TRACE_ID));
  }

  @Test
  void setsAndGetsSpanId() {
    LogContext.setSpanId("span-456");
    assertEquals("span-456", MDC.get(LogContext.SPAN_ID));
  }

  @Test
  void setsAndGetsServiceName() {
    LogContext.setServiceName("order-service");
    assertEquals("order-service", MDC.get(LogContext.SERVICE_NAME));
  }

  @Test
  void setsAndGetsCorrelationId() {
    LogContext.setCorrelationId("corr-789");
    assertEquals("corr-789", MDC.get(LogContext.CORRELATION_ID));
  }

  @Test
  void putAndGetCustomKey() {
    LogContext.put("orderId", "order-100");
    assertEquals("order-100", LogContext.get("orderId"));
  }

  @Test
  void removeKey() {
    LogContext.put("temp", "value");
    LogContext.remove("temp");
    assertNull(LogContext.get("temp"));
  }

  @Test
  void clearRemovesAllKeys() {
    LogContext.setUserId("user-1");
    LogContext.setRequestId("req-1");
    LogContext.clear();
    assertNull(LogContext.get(LogContext.USER_ID));
    assertNull(LogContext.get(LogContext.REQUEST_ID));
  }
}
