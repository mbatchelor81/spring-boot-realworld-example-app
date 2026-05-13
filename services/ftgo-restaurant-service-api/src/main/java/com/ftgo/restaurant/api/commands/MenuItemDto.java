package com.ftgo.restaurant.api.commands;

import com.ftgo.common.Money;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class MenuItemDto {

  @NotBlank private final String id;

  @NotBlank private final String name;

  @NotNull private final Money price;

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
