package com.ftgo.consumer.metrics;

import com.ftgo.tracing.TracingHelper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ConsumerMetrics {

  private final Counter consumersRegistered;
  private final Counter consumersUpdated;
  private final Counter consumerValidationFailures;
  private final TracingHelper tracingHelper;

  public ConsumerMetrics(MeterRegistry registry, TracingHelper tracingHelper) {
    this.tracingHelper = tracingHelper;

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

  public TracingHelper tracing() {
    return tracingHelper;
  }
}
