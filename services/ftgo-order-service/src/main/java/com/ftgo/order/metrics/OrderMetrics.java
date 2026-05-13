package com.ftgo.order.metrics;

import com.ftgo.tracing.TracingHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class OrderMetrics {

  private final Counter ordersCreated;
  private final Counter ordersAccepted;
  private final Counter ordersCancelled;
  private final Counter ordersRevised;
  private final Counter ordersFailed;
  private final Timer orderProcessingTime;
  private final TracingHelper tracingHelper;

  public OrderMetrics(MeterRegistry registry, TracingHelper tracingHelper) {
    this.tracingHelper = tracingHelper;

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

  public TracingHelper tracing() {
    return tracingHelper;
  }
}
