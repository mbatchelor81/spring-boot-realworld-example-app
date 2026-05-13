package com.ftgo.testlib.builders;

import static org.assertj.core.api.Assertions.assertThat;

import com.ftgo.domain.Consumer;
import org.junit.jupiter.api.Test;

class ConsumerBuilderTest {

  @Test
  void build_withDefaults_createsConsumerWithDefaultName() {
    Consumer consumer = ConsumerBuilder.aConsumer().build();

    assertThat(consumer.getName().getFirstName()).isEqualTo("John");
    assertThat(consumer.getName().getLastName()).isEqualTo("Doe");
  }

  @Test
  void build_withCustomName_createsConsumerWithCustomName() {
    Consumer consumer = ConsumerBuilder.aConsumer().withName("Jane", "Smith").build();

    assertThat(consumer.getName().getFirstName()).isEqualTo("Jane");
    assertThat(consumer.getName().getLastName()).isEqualTo("Smith");
  }
}
