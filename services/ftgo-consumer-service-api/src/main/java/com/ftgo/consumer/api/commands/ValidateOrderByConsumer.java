package com.ftgo.consumer.api.commands;

import com.ftgo.common.Money;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class ValidateOrderByConsumer {

  @Positive private final long consumerId;

  @Positive private final long orderId;

  @NotNull private final Money orderTotal;

  public ValidateOrderByConsumer(long consumerId, long orderId, Money orderTotal) {
    this.consumerId = consumerId;
    this.orderId = orderId;
    this.orderTotal = orderTotal;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public long getOrderId() {
    return orderId;
  }

  public Money getOrderTotal() {
    return orderTotal;
  }
}
