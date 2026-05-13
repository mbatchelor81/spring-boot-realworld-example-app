package com.ftgo.testlib.assertions;

import com.ftgo.common.Money;
import org.assertj.core.api.AbstractAssert;

/** AssertJ custom assertion for {@link Money} value objects. */
public class MoneyAssert extends AbstractAssert<MoneyAssert, Money> {

  public MoneyAssert(Money actual) {
    super(actual, MoneyAssert.class);
  }

  public static MoneyAssert assertThat(Money actual) {
    return new MoneyAssert(actual);
  }

  public MoneyAssert isPositive() {
    isNotNull();
    if (!actual.isGreaterThanOrEqual(Money.ZERO) || actual.equals(Money.ZERO)) {
      failWithMessage("Expected money to be positive but was <%s>", actual.asString());
    }
    return this;
  }

  public MoneyAssert isZero() {
    isNotNull();
    if (!actual.equals(Money.ZERO)) {
      failWithMessage("Expected money to be zero but was <%s>", actual.asString());
    }
    return this;
  }

  public MoneyAssert isEqualTo(String amount) {
    isNotNull();
    Money expected = new Money(amount);
    if (!actual.equals(expected)) {
      failWithMessage(
          "Expected money to be <%s> but was <%s>", expected.asString(), actual.asString());
    }
    return this;
  }

  public MoneyAssert isGreaterThanOrEqualTo(Money other) {
    isNotNull();
    if (!actual.isGreaterThanOrEqual(other)) {
      failWithMessage(
          "Expected money <%s> to be >= <%s>", actual.asString(), other.asString());
    }
    return this;
  }
}
