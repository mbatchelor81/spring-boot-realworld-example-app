package com.ftgo.order.api.events;

import com.ftgo.common.Money;

public class OrderCreatedEvent {

  private final long orderId;
  private final long consumerId;
  private final long restaurantId;
  private final Money orderTotal;

  public OrderCreatedEvent(
      long orderId, long consumerId, long restaurantId, Money orderTotal) {
    this.orderId = orderId;
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.orderTotal = orderTotal;
  }

  public long getOrderId() {
    return orderId;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }
}
