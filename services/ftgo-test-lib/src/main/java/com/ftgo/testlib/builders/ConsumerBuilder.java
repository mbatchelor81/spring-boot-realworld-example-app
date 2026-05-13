package com.ftgo.testlib.builders;

import com.ftgo.common.PersonName;
import com.ftgo.domain.Consumer;

/**
 * Test data builder for {@link Consumer} domain objects.
 *
 * <p>Usage:
 *
 * <pre>
 * Consumer consumer = ConsumerBuilder.aConsumer()
 *     .withName("Jane", "Doe")
 *     .build();
 * </pre>
 */
public final class ConsumerBuilder {

  private String firstName = "John";
  private String lastName = "Doe";

  private ConsumerBuilder() {}

  public static ConsumerBuilder aConsumer() {
    return new ConsumerBuilder();
  }

  public ConsumerBuilder withName(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    return this;
  }

  public ConsumerBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public ConsumerBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public Consumer build() {
    return new Consumer(new PersonName(firstName, lastName));
  }
}
