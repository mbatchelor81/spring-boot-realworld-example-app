package com.ftgo.consumer.infrastructure.health;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("consumerServiceHealth")
public class ConsumerServiceHealthIndicator implements HealthIndicator {

  private static final Logger log = LoggerFactory.getLogger(ConsumerServiceHealthIndicator.class);

  private final DataSource dataSource;

  public ConsumerServiceHealthIndicator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Health health() {
    try (var connection = dataSource.getConnection()) {
      boolean valid = connection.isValid(2);
      if (valid) {
        return Health.up()
            .withDetail("service", "ftgo-consumer-service")
            .withDetail("database", "connected")
            .build();
      }
      return Health.down()
          .withDetail("service", "ftgo-consumer-service")
          .withDetail("database", "connection invalid")
          .build();
    } catch (Exception e) {
      log.error("Consumer service health check failed", e);
      return Health.down()
          .withDetail("service", "ftgo-consumer-service")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
