package com.ftgo.restaurant.api.events;

public class RestaurantCreatedEvent {

  private final long restaurantId;
  private final String name;

  public RestaurantCreatedEvent(long restaurantId, String name) {
    this.restaurantId = restaurantId;
    this.name = name;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public String getName() {
    return name;
  }
}
