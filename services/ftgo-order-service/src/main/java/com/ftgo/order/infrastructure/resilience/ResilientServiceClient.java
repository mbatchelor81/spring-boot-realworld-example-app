package com.ftgo.order.infrastructure.resilience;

import com.ftgo.order.infrastructure.discovery.ServiceDiscoveryProperties;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ResilientServiceClient {

  private static final Logger log = LoggerFactory.getLogger(ResilientServiceClient.class);

  private final RestTemplate restTemplate;
  private final ServiceDiscoveryProperties discoveryProperties;

  public ResilientServiceClient(
      RestTemplate restTemplate, ServiceDiscoveryProperties discoveryProperties) {
    this.restTemplate = restTemplate;
    this.discoveryProperties = discoveryProperties;
  }

  @CircuitBreaker(name = "consumerService")
  @Retry(name = "consumerService", fallbackMethod = "validateConsumerFallback")
  @Bulkhead(name = "consumerService")
  public boolean validateOrderForConsumer(long consumerId, long orderId) {
    String url =
        discoveryProperties.getConsumerServiceUrl() + "/api/consumers/" + consumerId;
    log.debug("Validating consumer {} for order {}", consumerId, orderId);
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  @CircuitBreaker(name = "restaurantService")
  @Retry(name = "restaurantService", fallbackMethod = "validateRestaurantFallback")
  @Bulkhead(name = "restaurantService")
  public boolean validateRestaurant(long restaurantId) {
    String url =
        discoveryProperties.getRestaurantServiceUrl() + "/api/restaurants/" + restaurantId;
    log.debug("Validating restaurant {}", restaurantId);
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    return response.getStatusCode().is2xxSuccessful();
  }

  boolean validateConsumerFallback(long consumerId, long orderId, Throwable t) {
    log.warn(
        "Consumer service unavailable for consumer {}. Allowing order {} to proceed. Error: {}",
        consumerId,
        orderId,
        t.getMessage());
    return true;
  }

  boolean validateRestaurantFallback(long restaurantId, Throwable t) {
    log.warn(
        "Restaurant service unavailable for restaurant {}. Allowing operation to proceed. Error: {}",
        restaurantId,
        t.getMessage());
    return true;
  }
}
