package com.ftgo.testlib.fixtures;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;
import com.ftgo.domain.OrderLineItem;
import com.ftgo.testlib.builders.OrderBuilder;

/** Pre-built {@link Order} instances for common test scenarios. */
public final class OrderFixtures {

  private OrderFixtures() {}

  public static Order approvedOrder() {
    return OrderBuilder.anOrder()
        .forConsumerId(1L)
        .withRestaurant(RestaurantFixtures.ajantaRestaurant())
        .withLineItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"), 1)
        .build();
  }

  public static Order multiItemOrder() {
    return OrderBuilder.anOrder()
        .forConsumerId(1L)
        .withRestaurant(RestaurantFixtures.ajantaRestaurant())
        .withLineItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"), 2)
        .withLineItem("vindaloo-1", "Lamb Vindaloo", new Money("14.99"), 1)
        .build();
  }

  public static OrderLineItem chickenTikkaMasala() {
    return new OrderLineItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"), 1);
  }

  public static OrderLineItem chickenTikkaMasala(int quantity) {
    return new OrderLineItem("tikka-1", "Chicken Tikka Masala", new Money("12.99"), quantity);
  }
}
