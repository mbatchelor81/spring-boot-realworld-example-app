package com.ftgo.courier.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class CourierMetrics {

  private final Counter couriersCreated;
  private final Counter couriersAvailable;
  private final Counter couriersUnavailable;
  private final Counter deliveriesCompleted;
  private final Tracer tracer;

  public CourierMetrics(MeterRegistry registry, Tracer tracer) {
    this.tracer = tracer;

    this.couriersCreated =
        Counter.builder("ftgo.couriers.created")
            .description("Total number of couriers created")
            .register(registry);

    this.couriersAvailable =
        Counter.builder("ftgo.couriers.available")
            .description("Total times a courier became available")
            .register(registry);

    this.couriersUnavailable =
        Counter.builder("ftgo.couriers.unavailable")
            .description("Total times a courier became unavailable")
            .register(registry);

    this.deliveriesCompleted =
        Counter.builder("ftgo.deliveries.completed")
            .description("Total number of deliveries completed")
            .register(registry);
  }

  public void incrementCreated() {
    couriersCreated.increment();
  }

  public void incrementAvailable() {
    couriersAvailable.increment();
  }

  public void incrementUnavailable() {
    couriersUnavailable.increment();
  }

  public void incrementDeliveriesCompleted() {
    deliveriesCompleted.increment();
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
