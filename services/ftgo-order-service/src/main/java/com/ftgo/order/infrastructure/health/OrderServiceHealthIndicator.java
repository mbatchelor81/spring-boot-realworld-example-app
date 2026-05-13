package com.ftgo.order.infrastructure.health;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("orderServiceHealth")
public class OrderServiceHealthIndicator implements HealthIndicator {

  private static final Logger log = LoggerFactory.getLogger(OrderServiceHealthIndicator.class);

  private final DataSource dataSource;

  public OrderServiceHealthIndicator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Health health() {
    try (var connection = dataSource.getConnection()) {
      boolean valid = connection.isValid(2);
      if (valid) {
        return Health.up()
            .withDetail("service", "ftgo-order-service")
            .withDetail("database", "connected")
            .build();
      }
      return Health.down()
          .withDetail("service", "ftgo-order-service")
          .withDetail("database", "connection invalid")
          .build();
    } catch (Exception e) {
      log.error("Order service health check failed", e);
      return Health.down()
          .withDetail("service", "ftgo-order-service")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
