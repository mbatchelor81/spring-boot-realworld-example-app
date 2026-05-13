package com.ftgo.courier.api.events;

public class CourierAvailableEvent {

  private final long courierId;

  public CourierAvailableEvent(long courierId) {
    this.courierId = courierId;
  }

  public long getCourierId() {
    return courierId;
  }
}
