package com.ftgo.logging;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private CorrelationIdFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = new CorrelationIdFilter("test-service");
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    filterChain = new MockFilterChain();
  }

  @AfterEach
  void tearDown() {
    MDC.clear();
  }

  @Test
  void generatesCorrelationId_whenHeaderMissing() throws ServletException, IOException {
    filter.doFilter(request, response, filterChain);

    String headerValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    assertNotNull(headerValue);
    assertFalse(headerValue.isBlank());
  }

  @Test
  void usesExistingCorrelationId_whenHeaderPresent() throws ServletException, IOException {
    String existingId = "existing-correlation-id-123";
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);

    filter.doFilter(request, response, filterChain);

    assertEquals(existingId, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
  }

  @Test
  void generatesNewId_whenHeaderIsBlank() throws ServletException, IOException {
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

    filter.doFilter(request, response, filterChain);

    String headerValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);
    assertNotNull(headerValue);
    assertNotEquals("   ", headerValue);
    assertFalse(headerValue.isBlank());
  }

  @Test
  void setsMdcFields_duringFilterChain() throws ServletException, IOException {
    String existingId = "trace-abc-123";
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);

    FilterChain capturingChain =
        (req, res) -> {
          assertEquals(existingId, MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
          assertEquals("test-service", MDC.get(CorrelationIdFilter.SERVICE_NAME_MDC_KEY));
        };

    filter.doFilter(request, response, capturingChain);
  }

  @Test
  void clearsMdcFields_afterFilterChainCompletes() throws ServletException, IOException {
    filter.doFilter(request, response, filterChain);

    assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    assertNull(MDC.get(CorrelationIdFilter.SERVICE_NAME_MDC_KEY));
  }

  @Test
  void clearsMdcFields_whenFilterChainThrows() {
    FilterChain throwingChain =
        (req, res) -> {
          throw new ServletException("test error");
        };

    assertThrows(
        ServletException.class, () -> filter.doFilter(request, response, throwingChain));

    assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    assertNull(MDC.get(CorrelationIdFilter.SERVICE_NAME_MDC_KEY));
  }
}
