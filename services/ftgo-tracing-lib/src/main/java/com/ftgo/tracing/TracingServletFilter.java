package com.ftgo.tracing;

import brave.Span;
import brave.Tracing;
import brave.propagation.TraceContextOrSamplingFlags;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class TracingServletFilter implements Filter {

  private final Tracing tracing;
  private final brave.Tracer tracer;

  public TracingServletFilter(Tracing tracing) {
    this.tracing = tracing;
    this.tracer = tracing.tracer();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    TraceContextOrSamplingFlags extracted =
        tracing
            .propagation()
            .extractor(
                (carrier, key) -> ((HttpServletRequest) carrier).getHeader(key))
            .extract(httpRequest);

    Span span = tracer.nextSpan(extracted);
    span.name(httpRequest.getMethod() + " " + httpRequest.getRequestURI());
    span.kind(Span.Kind.SERVER);
    span.tag("http.method", httpRequest.getMethod());
    span.tag("http.url", httpRequest.getRequestURL().toString());
    span.start();

    try (brave.Tracer.SpanInScope scope = tracer.withSpanInScope(span)) {
      chain.doFilter(request, response);
      span.tag("http.status_code", String.valueOf(httpResponse.getStatus()));
    } catch (Exception e) {
      span.error(e);
      span.tag("http.status_code", "500");
      throw e;
    } finally {
      span.finish();
    }
  }
}
