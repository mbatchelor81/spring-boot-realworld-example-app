package com.ftgo.testlib.builders;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;
import com.ftgo.domain.OrderState;
import org.junit.jupiter.api.Test;

class OrderBuilderTest {

  @Test
  void build_withDefaults_createsApprovedOrder() {
    Order order = OrderBuilder.anOrder().build();

    assertThat(order.getOrderState()).isEqualTo(OrderState.APPROVED);
    assertThat(order.getConsumerId()).isEqualTo(1L);
    assertThat(order.getLineItems()).isNotEmpty();
  }

  @Test
  void build_withCustomLineItems_createsOrderWithCorrectItems() {
    Order order =
        OrderBuilder.anOrder()
            .forConsumerId(42L)
            .withLineItem("item-1", "Pizza", new Money("15.00"), 2)
            .build();

    assertThat(order.getConsumerId()).isEqualTo(42L);
    assertThat(order.getLineItems()).hasSize(1);
    assertThat(order.getLineItems().get(0).getName()).isEqualTo("Pizza");
  }
}
