package com.ftgo.testlib.builders;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;
import com.ftgo.domain.OrderLineItem;
import com.ftgo.domain.Restaurant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test data builder for {@link Order} domain objects.
 *
 * <p>Usage:
 *
 * <pre>
 * Order order = OrderBuilder.anOrder()
 *     .forConsumerId(1L)
 *     .withRestaurant(RestaurantBuilder.aRestaurant().build())
 *     .withLineItem("1", "Chicken Tikka Masala", new Money("12.99"), 2)
 *     .build();
 * </pre>
 */
public final class OrderBuilder {

  private long consumerId = 1L;
  private Restaurant restaurant;
  private final List<OrderLineItem> lineItems = new ArrayList<>();

  private OrderBuilder() {}

  public static OrderBuilder anOrder() {
    return new OrderBuilder();
  }

  public OrderBuilder forConsumerId(long consumerId) {
    this.consumerId = consumerId;
    return this;
  }

  public OrderBuilder withRestaurant(Restaurant restaurant) {
    this.restaurant = restaurant;
    return this;
  }

  public OrderBuilder withLineItem(String menuItemId, String name, Money price, int quantity) {
    this.lineItems.add(new OrderLineItem(menuItemId, name, price, quantity));
    return this;
  }

  public OrderBuilder withLineItems(OrderLineItem... items) {
    this.lineItems.addAll(Arrays.asList(items));
    return this;
  }

  public Order build() {
    Restaurant effectiveRestaurant =
        restaurant != null ? restaurant : RestaurantBuilder.aRestaurant().build();

    List<OrderLineItem> effectiveLineItems = lineItems;
    if (effectiveLineItems.isEmpty()) {
      effectiveLineItems = new ArrayList<>();
      effectiveLineItems.add(new OrderLineItem("item-1", "Default Item", new Money("9.99"), 1));
    }

    return new Order(consumerId, effectiveRestaurant, effectiveLineItems);
  }
}
