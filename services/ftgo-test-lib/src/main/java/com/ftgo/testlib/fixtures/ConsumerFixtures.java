package com.ftgo.testlib.fixtures;

import com.ftgo.common.PersonName;
import com.ftgo.domain.Consumer;
import com.ftgo.testlib.builders.ConsumerBuilder;

/** Pre-built {@link Consumer} instances for common test scenarios. */
public final class ConsumerFixtures {

  private ConsumerFixtures() {}

  public static Consumer johndoe() {
    return ConsumerBuilder.aConsumer().withName("John", "Doe").build();
  }

  public static Consumer janedoe() {
    return ConsumerBuilder.aConsumer().withName("Jane", "Doe").build();
  }

  public static PersonName defaultName() {
    return new PersonName("John", "Doe");
  }
}
