package com.ftgo.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;

public class TracingHelper {

  private final Tracer tracer;

  public TracingHelper(Tracer tracer) {
    this.tracer = tracer;
  }

  public <T> T traceOperation(String operationName, Supplier<T> operation) {
    Span span = tracer.nextSpan().name(operationName).start();
    try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
      T result = operation.get();
      span.event("completed");
      return result;
    } catch (Exception e) {
      span.error(e);
      throw e;
    } finally {
      span.end();
    }
  }

  public void traceOperation(String operationName, Runnable operation) {
    Span span = tracer.nextSpan().name(operationName).start();
    try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
      operation.run();
      span.event("completed");
    } catch (Exception e) {
      span.error(e);
      throw e;
    } finally {
      span.end();
    }
  }
}
