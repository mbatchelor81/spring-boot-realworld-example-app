package com.ftgo.courier.api.commands;

import com.ftgo.common.Address;
import java.time.LocalDateTime;

public class ScheduleDeliveryCommand {

  private final long orderId;
  private final long courierId;
  private final Address pickupAddress;
  private final Address deliveryAddress;
  private final LocalDateTime deliveryTime;

  public ScheduleDeliveryCommand(
      long orderId,
      long courierId,
      Address pickupAddress,
      Address deliveryAddress,
      LocalDateTime deliveryTime) {
    this.orderId = orderId;
    this.courierId = courierId;
    this.pickupAddress = pickupAddress;
    this.deliveryAddress = deliveryAddress;
    this.deliveryTime = deliveryTime;
  }

  public long getOrderId() {
    return orderId;
  }

  public long getCourierId() {
    return courierId;
  }

  public Address getPickupAddress() {
    return pickupAddress;
  }

  public Address getDeliveryAddress() {
    return deliveryAddress;
  }

  public LocalDateTime getDeliveryTime() {
    return deliveryTime;
  }
}
