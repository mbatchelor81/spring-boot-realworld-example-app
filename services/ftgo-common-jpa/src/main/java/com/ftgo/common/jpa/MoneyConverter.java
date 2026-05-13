package com.ftgo.common.jpa;

import com.ftgo.common.Money;
import java.math.BigDecimal;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA {@link AttributeConverter} that persists {@link Money} as a {@link BigDecimal} column. Apply
 * via {@code @Convert(converter = MoneyConverter.class)} on entity fields when the default
 * {@code @Embeddable} mapping is not appropriate.
 */
@Converter
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

  @Override
  public BigDecimal convertToDatabaseColumn(Money money) {
    return money == null ? null : new BigDecimal(money.asString());
  }

  @Override
  public Money convertToEntityAttribute(BigDecimal dbValue) {
    return dbValue == null ? null : new Money(dbValue);
  }
}
