package com.ftgo.testlib.builders;

import com.ftgo.common.Money;
import com.ftgo.domain.MenuItem;

/**
 * Test data builder for {@link MenuItem} domain objects.
 *
 * <p>Usage:
 *
 * <pre>
 * MenuItem item = MenuItemBuilder.aMenuItem()
 *     .withId("tikka-1")
 *     .withName("Chicken Tikka Masala")
 *     .withPrice(new Money("12.99"))
 *     .build();
 * </pre>
 */
public final class MenuItemBuilder {

  private String id = "item-1";
  private String name = "Default Menu Item";
  private Money price = new Money("9.99");

  private MenuItemBuilder() {}

  public static MenuItemBuilder aMenuItem() {
    return new MenuItemBuilder();
  }

  public MenuItemBuilder withId(String id) {
    this.id = id;
    return this;
  }

  public MenuItemBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public MenuItemBuilder withPrice(Money price) {
    this.price = price;
    return this;
  }

  public MenuItemBuilder withPrice(String amount) {
    this.price = new Money(amount);
    return this;
  }

  public MenuItem build() {
    return new MenuItem(id, name, price);
  }
}
