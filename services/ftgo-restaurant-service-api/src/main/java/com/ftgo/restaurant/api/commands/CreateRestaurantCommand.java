package com.ftgo.restaurant.api.commands;

import com.ftgo.common.Address;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class CreateRestaurantCommand {

  @NotBlank private final String name;

  @NotNull @Valid private final Address address;

  @NotEmpty @Valid private final List<MenuItemDto> menuItems;

  public CreateRestaurantCommand(String name, Address address, List<MenuItemDto> menuItems) {
    this.name = name;
    this.address = address;
    this.menuItems = menuItems;
  }

  public String getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }

  public List<MenuItemDto> getMenuItems() {
    return menuItems;
  }
}
