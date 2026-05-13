package com.ftgo.testlib.assertions;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;

/** Entry point for FTGO domain-specific AssertJ assertions. */
public final class FtgoAssertions {

  private FtgoAssertions() {}

  public static OrderAssert assertThat(Order order) {
    return new OrderAssert(order);
  }

  public static MoneyAssert assertThat(Money money) {
    return new MoneyAssert(money);
  }
}
