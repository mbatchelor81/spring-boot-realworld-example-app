package com.ftgo.common;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MoneySerializationTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeAll
  static void initialize() {
    objectMapper.registerModule(new MoneyModule());
  }

  public static class MoneyContainer {

    private Money price;

    @Override
    public boolean equals(Object o) {
      return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

    public Money getPrice() {
      return price;
    }

    public void setPrice(Money price) {
      this.price = price;
    }

    public MoneyContainer() {}

    public MoneyContainer(Money price) {
      this.price = price;
    }
  }

  @Test
  void shouldSerialize() throws Exception {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals("{\"price\":\"12.34\"}", objectMapper.writeValueAsString(mc));
  }

  @Test
  void shouldDeserialize() throws Exception {
    Money price = new Money("12.34");
    MoneyContainer mc = new MoneyContainer(price);
    assertEquals(mc, objectMapper.readValue("{\"price\":\"12.34\"}", MoneyContainer.class));
  }

  @Test
  void shouldFailToDeserializeObject() throws Exception {
    assertThrows(
        JsonMappingException.class,
        () ->
            objectMapper.readValue(
                "{\"price\": { \"amount\" : \"12.34\"} }", MoneyContainer.class));
  }
}
