package com.ftgo.restaurant.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMetrics {

  private final Counter restaurantsCreated;
  private final Counter menuRevisions;
  private final Counter restaurantLookupFailures;

  public RestaurantMetrics(MeterRegistry registry) {
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
}
