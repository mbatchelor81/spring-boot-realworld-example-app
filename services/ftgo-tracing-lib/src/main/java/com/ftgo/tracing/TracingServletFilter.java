package com.ftgo.tracing;

import brave.Span;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContextOrSamplingFlags;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TracingServletFilter implements Filter {

  private final brave.Tracer tracer;
  private final Propagation.Getter<HttpServletRequest, String> getter =
      (carrier, key) -> carrier.getHeader(key);
  private final Propagation<String> propagation;

  public TracingServletFilter(Tracing tracing) {
    this.tracer = tracing.tracer();
    this.propagation = tracing.propagation();
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

    TraceContextOrSamplingFlags extracted = propagation.extractor(getter).extract(httpRequest);

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
