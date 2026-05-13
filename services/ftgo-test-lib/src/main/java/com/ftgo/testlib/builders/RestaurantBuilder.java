package com.ftgo.testlib.builders;

import com.ftgo.common.Address;
import com.ftgo.common.Money;
import com.ftgo.domain.MenuItem;
import com.ftgo.domain.Restaurant;
import com.ftgo.domain.RestaurantMenu;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test data builder for {@link Restaurant} domain objects.
 *
 * <p>Usage:
 *
 * <pre>
 * Restaurant restaurant = RestaurantBuilder.aRestaurant()
 *     .withName("Ajanta")
 *     .withMenuItem("1", "Chicken Tikka Masala", new Money("12.99"))
 *     .build();
 * </pre>
 */
public final class RestaurantBuilder {

  private String name = "Test Restaurant";
  private Address address = new Address("123 Main St", null, "Oakland", "CA", "94611");
  private final List<MenuItem> menuItems = new ArrayList<>();

  private RestaurantBuilder() {}

  public static RestaurantBuilder aRestaurant() {
    return new RestaurantBuilder();
  }

  public RestaurantBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public RestaurantBuilder withAddress(Address address) {
    this.address = address;
    return this;
  }

  public RestaurantBuilder withAddress(
      String street1, String street2, String city, String state, String zip) {
    this.address = new Address(street1, street2, city, state, zip);
    return this;
  }

  public RestaurantBuilder withMenuItem(String id, String name, Money price) {
    this.menuItems.add(new MenuItem(id, name, price));
    return this;
  }

  public RestaurantBuilder withMenuItems(MenuItem... items) {
    this.menuItems.addAll(Arrays.asList(items));
    return this;
  }

  public Restaurant build() {
    List<MenuItem> effectiveItems = menuItems;
    if (effectiveItems.isEmpty()) {
      effectiveItems = new ArrayList<>();
      effectiveItems.add(new MenuItem("item-1", "Default Item", new Money("9.99")));
    }
    return new Restaurant(name, address, new RestaurantMenu(effectiveItems));
  }
}
