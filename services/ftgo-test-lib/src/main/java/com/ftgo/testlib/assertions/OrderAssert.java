package com.ftgo.testlib.assertions;

import com.ftgo.common.Money;
import com.ftgo.domain.Order;
import com.ftgo.domain.OrderState;
import org.assertj.core.api.AbstractAssert;

/** AssertJ custom assertion for {@link Order} domain objects. */
public class OrderAssert extends AbstractAssert<OrderAssert, Order> {

  public OrderAssert(Order actual) {
    super(actual, OrderAssert.class);
  }

  public static OrderAssert assertThat(Order actual) {
    return new OrderAssert(actual);
  }

  public OrderAssert isInState(OrderState expected) {
    isNotNull();
    if (actual.getOrderState() != expected) {
      failWithMessage(
          "Expected order to be in state <%s> but was <%s>",
          expected, actual.getOrderState());
    }
    return this;
  }

  public OrderAssert isApproved() {
    return isInState(OrderState.APPROVED);
  }

  public OrderAssert isCancelled() {
    return isInState(OrderState.CANCELLED);
  }

  public OrderAssert hasConsumerId(long expectedConsumerId) {
    isNotNull();
    if (!actual.getConsumerId().equals(expectedConsumerId)) {
      failWithMessage(
          "Expected order consumerId to be <%d> but was <%d>",
          expectedConsumerId, actual.getConsumerId());
    }
    return this;
  }

  public OrderAssert hasTotalGreaterThan(Money minimum) {
    isNotNull();
    if (!actual.getOrderTotal().isGreaterThanOrEqual(minimum)
        || actual.getOrderTotal().equals(minimum)) {
      failWithMessage(
          "Expected order total to be > <%s> but was <%s>",
          minimum.asString(), actual.getOrderTotal().asString());
    }
    return this;
  }

  public OrderAssert hasLineItemCount(int expectedCount) {
    isNotNull();
    int actualCount = actual.getLineItems().size();
    if (actualCount != expectedCount) {
      failWithMessage(
          "Expected order to have <%d> line items but had <%d>", expectedCount, actualCount);
    }
    return this;
  }
}
