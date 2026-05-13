package com.ftgo.restaurant.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMetrics {

  private final Counter restaurantsCreated;
  private final Counter menuRevisions;
  private final Counter restaurantLookupFailures;
  private final Tracer tracer;

  public RestaurantMetrics(MeterRegistry registry, Tracer tracer) {
    this.tracer = tracer;

    this.restaurantsCreated =
        Counter.builder("ftgo.restaurants.created")
            .description("Total number of restaurants created")
            .register(registry);

    this.menuRevisions =
        Counter.builder("ftgo.restaurants.menu.revisions")
            .description("Total number of menu revisions")
            .register(registry);

    this.restaurantLookupFailures =
        Counter.builder("ftgo.restaurants.lookup.failures")
            .description("Total number of restaurant lookup failures")
            .register(registry);
  }

  public void incrementCreated() {
    restaurantsCreated.increment();
  }

  public void incrementMenuRevisions() {
    menuRevisions.increment();
  }

  public void incrementLookupFailures() {
    restaurantLookupFailures.increment();
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
