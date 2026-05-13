package com.ftgo.order.api.commands;

public class OrderLineItemDto {

  private final String menuItemId;
  private final int quantity;

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
