package com.ftgo.order.infrastructure.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ftgo.service-discovery")
public class ServiceDiscoveryProperties {

  private String consumerServiceUrl = "http://ftgo-consumer-service:8080";
  private String restaurantServiceUrl = "http://ftgo-restaurant-service:8080";

  public String getConsumerServiceUrl() {
    return consumerServiceUrl;
  }

  public void setConsumerServiceUrl(String consumerServiceUrl) {
    this.consumerServiceUrl = consumerServiceUrl;
  }

  public String getRestaurantServiceUrl() {
    return restaurantServiceUrl;
  }

  public void setRestaurantServiceUrl(String restaurantServiceUrl) {
    this.restaurantServiceUrl = restaurantServiceUrl;
  }
}
