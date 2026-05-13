package com.ftgo.courier.api.commands;

import com.ftgo.common.Address;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class ScheduleDeliveryCommand {

  @Positive private final long orderId;

  @Positive private final long courierId;

  @NotNull @Valid private final Address pickupAddress;

  @NotNull @Valid private final Address deliveryAddress;

  @NotNull private final LocalDateTime deliveryTime;

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
