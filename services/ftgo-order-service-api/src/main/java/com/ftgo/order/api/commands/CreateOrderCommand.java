package com.ftgo.order.api.commands;

import com.ftgo.common.Address;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class CreateOrderCommand {

  @Positive private final long consumerId;

  @Positive private final long restaurantId;

  @NotNull @Valid private final Address deliveryAddress;

  @NotEmpty @Valid private final List<OrderLineItemDto> lineItems;

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
