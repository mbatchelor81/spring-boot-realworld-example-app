package com.ftgo.common;

import java.math.BigDecimal;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Embeddable
@Access(AccessType.FIELD)
public class Money {

  public static final Money ZERO = new Money(0);

  private BigDecimal amount;

  private Money() {}

  public Money(BigDecimal amount) {
    this.amount = amount;
  }

  public Money(String s) {
    this.amount = new BigDecimal(s);
  }

  public Money(int i) {
    this.amount = new BigDecimal(i);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return amount.compareTo(money.amount) == 0;
  }

  @Override
  public int hashCode() {
    String normalized = amount.signum() == 0 ? "0" : amount.stripTrailingZeros().toPlainString();
    return new HashCodeBuilder(17, 37).append(normalized).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("amount", amount).toString();
  }

  public Money add(Money delta) {
    return new Money(amount.add(delta.amount));
  }

  public boolean isGreaterThanOrEqual(Money other) {
    return amount.compareTo(other.amount) >= 0;
  }

  public String asString() {
    return amount.toPlainString();
  }

  public Money multiply(int x) {
    return new Money(amount.multiply(new BigDecimal(x)));
  }
}
