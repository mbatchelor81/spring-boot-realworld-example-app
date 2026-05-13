package com.ftgo.order.api.commands;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

public class OrderLineItemDto {

  @NotBlank private final String menuItemId;

  @Positive private final int quantity;

  public OrderLineItemDto(String menuItemId, int quantity) {
    this.menuItemId = menuItemId;
    this.quantity = quantity;
  }

  public String getMenuItemId() {
    return menuItemId;
  }

  public int getQuantity() {
    return quantity;
  }
}
