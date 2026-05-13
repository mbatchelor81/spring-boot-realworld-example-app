package com.ftgo.restaurant.api.commands;

import com.ftgo.common.Address;
import java.util.List;

public class CreateRestaurantCommand {

  private final String name;
  private final Address address;
  private final List<MenuItemDto> menuItems;

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
