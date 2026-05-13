package com.ftgo.restaurant.api.commands;

import com.ftgo.common.Money;

public class MenuItemDto {

  private final String id;
  private final String name;
  private final Money price;

  public MenuItemDto(String id, String name, Money price) {
    this.id = id;
    this.name = name;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Money getPrice() {
    return price;
  }
}
