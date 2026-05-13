package com.ftgo.order.infrastructure.health;

import com.ftgo.order.infrastructure.discovery.ServiceDiscoveryProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component("downstreamServices")
public class DownstreamServicesHealthIndicator implements HealthIndicator {

  private static final Logger log =
      LoggerFactory.getLogger(DownstreamServicesHealthIndicator.class);

  private final RestTemplate restTemplate;
  private final ServiceDiscoveryProperties discoveryProperties;

  public DownstreamServicesHealthIndicator(
      RestTemplate restTemplate, ServiceDiscoveryProperties discoveryProperties) {
    this.restTemplate = restTemplate;
    this.discoveryProperties = discoveryProperties;
  }

  @Override
  public Health health() {
    Map<String, Object> details = new LinkedHashMap<>();
    boolean allHealthy = true;

    allHealthy &= checkService("consumerService", discoveryProperties.getConsumerServiceUrl(), details);
    allHealthy &=
        checkService("restaurantService", discoveryProperties.getRestaurantServiceUrl(), details);

    if (allHealthy) {
      return Health.up().withDetails(details).build();
    }
    return Health.down().withDetails(details).build();
  }

  private boolean checkService(String name, String baseUrl, Map<String, Object> details) {
    try {
      String healthUrl = baseUrl + "/actuator/health";
      restTemplate.getForEntity(healthUrl, String.class);
      details.put(name, "UP");
      return true;
    } catch (Exception e) {
      log.warn("Downstream service {} is unavailable: {}", name, e.getMessage());
      details.put(name, "DOWN - " + e.getMessage());
      return false;
    }
  }
}
