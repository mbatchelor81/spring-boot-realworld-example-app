package com.ftgo.order.api.events;

public class OrderStateChangedEvent {

  private final long orderId;
  private final String previousState;
  private final String newState;

  public OrderStateChangedEvent(long orderId, String previousState, String newState) {
    this.orderId = orderId;
    this.previousState = previousState;
    this.newState = newState;
  }

  public long getOrderId() {
    return orderId;
  }

  public String getPreviousState() {
    return previousState;
  }

  public String getNewState() {
    return newState;
  }
}
