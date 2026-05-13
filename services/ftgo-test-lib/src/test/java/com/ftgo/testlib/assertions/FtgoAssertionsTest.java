package com.ftgo.testlib.assertions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ftgo.common.Money;
import com.ftgo.testlib.builders.OrderBuilder;
import com.ftgo.testlib.fixtures.OrderFixtures;
import org.junit.jupiter.api.Test;

class FtgoAssertionsTest {

  @Test
  void orderAssert_approvedOrder_passesIsApproved() {
    var order = OrderFixtures.approvedOrder();
    FtgoAssertions.assertThat(order).isApproved();
  }

  @Test
  void orderAssert_approvedOrder_failsIsCancelled() {
    var order = OrderFixtures.approvedOrder();
    assertThatThrownBy(() -> FtgoAssertions.assertThat(order).isCancelled())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void moneyAssert_positiveMoney_passesIsPositive() {
    var money = new Money("25.99");
    FtgoAssertions.assertThat(money).isPositive();
  }

  @Test
  void moneyAssert_fractionalPositive_passesIsPositive() {
    var money = new Money("0.50");
    FtgoAssertions.assertThat(money).isPositive();
  }

  @Test
  void moneyAssert_zero_failsIsPositive() {
    assertThatThrownBy(() -> FtgoAssertions.assertThat(Money.ZERO).isPositive())
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void moneyAssert_zero_passesIsZero() {
    FtgoAssertions.assertThat(Money.ZERO).isZero();
  }

  @Test
  void moneyAssert_isEqualTo_matchingAmount() {
    var money = new Money("12.99");
    FtgoAssertions.assertThat(money).isEqualTo("12.99");
  }

  @Test
  void orderAssert_multiItemOrder_hasCorrectLineItemCount() {
    var order = OrderFixtures.multiItemOrder();
    FtgoAssertions.assertThat(order).hasLineItemCount(2);
  }

  @Test
  void orderAssert_hasTotalGreaterThan_zero() {
    var order =
        OrderBuilder.anOrder()
            .withLineItem("1", "Pizza", new Money("15.00"), 1)
            .build();
    FtgoAssertions.assertThat(order).hasTotalGreaterThan(Money.ZERO);
  }
}
