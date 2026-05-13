package com.ftgo.order.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

  private final Counter ordersCreated;
  private final Counter ordersAccepted;
  private final Counter ordersCancelled;
  private final Counter ordersRevised;
  private final Counter ordersFailed;
  private final Timer orderProcessingTime;
  private final Tracer tracer;

  public OrderMetrics(MeterRegistry registry, Tracer tracer) {
    this.tracer = tracer;

    this.ordersCreated =
        Counter.builder("ftgo.orders.created")
            .description("Total number of orders created")
            .register(registry);

    this.ordersAccepted =
        Counter.builder("ftgo.orders.accepted")
            .description("Total number of orders accepted by restaurants")
            .register(registry);

    this.ordersCancelled =
        Counter.builder("ftgo.orders.cancelled")
            .description("Total number of orders cancelled")
            .register(registry);

    this.ordersRevised =
        Counter.builder("ftgo.orders.revised")
            .description("Total number of order revisions")
            .register(registry);

    this.ordersFailed =
        Counter.builder("ftgo.orders.failed")
            .description("Total number of failed orders")
            .register(registry);

    this.orderProcessingTime =
        Timer.builder("ftgo.orders.processing.time")
            .description("Time taken to process an order")
            .register(registry);
  }

  public void incrementCreated() {
    ordersCreated.increment();
  }

  public void incrementAccepted() {
    ordersAccepted.increment();
  }

  public void incrementCancelled() {
    ordersCancelled.increment();
  }

  public void incrementRevised() {
    ordersRevised.increment();
  }

  public void incrementFailed() {
    ordersFailed.increment();
  }

  public Timer getOrderProcessingTimer() {
    return orderProcessingTime;
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
