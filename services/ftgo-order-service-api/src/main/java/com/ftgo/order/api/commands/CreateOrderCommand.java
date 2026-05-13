package com.ftgo.order.api.commands;

import com.ftgo.common.Address;
import java.util.List;

public class CreateOrderCommand {

  private final long consumerId;
  private final long restaurantId;
  private final Address deliveryAddress;
  private final List<OrderLineItemDto> lineItems;

  public CreateOrderCommand(
      long consumerId,
      long restaurantId,
      Address deliveryAddress,
      List<OrderLineItemDto> lineItems) {
    this.consumerId = consumerId;
    this.restaurantId = restaurantId;
    this.deliveryAddress = deliveryAddress;
    this.lineItems = lineItems;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public long getRestaurantId() {
    return restaurantId;
  }

  public Address getDeliveryAddress() {
    return deliveryAddress;
  }

  public List<OrderLineItemDto> getLineItems() {
    return lineItems;
  }
}
