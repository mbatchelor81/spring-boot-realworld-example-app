package com.ftgo.consumer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ConsumerMetrics {

  private final Counter consumersRegistered;
  private final Counter consumersUpdated;
  private final Counter consumerValidationFailures;
  private final Tracer tracer;

  public ConsumerMetrics(MeterRegistry registry, Tracer tracer) {
    this.tracer = tracer;

    this.consumersRegistered =
        Counter.builder("ftgo.consumers.registered")
            .description("Total number of consumers registered")
            .register(registry);

    this.consumersUpdated =
        Counter.builder("ftgo.consumers.updated")
            .description("Total number of consumer profile updates")
            .register(registry);

    this.consumerValidationFailures =
        Counter.builder("ftgo.consumers.validation.failures")
            .description("Total number of consumer validation failures")
            .register(registry);
  }

  public void incrementRegistered() {
    consumersRegistered.increment();
  }

  public void incrementUpdated() {
    consumersUpdated.increment();
  }

  public void incrementValidationFailures() {
    consumerValidationFailures.increment();
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
