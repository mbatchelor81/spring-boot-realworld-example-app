package com.ftgo.consumer.api.events;

public class ConsumerCreatedEvent {

  private final long consumerId;
  private final String firstName;
  private final String lastName;

  public ConsumerCreatedEvent(long consumerId, String firstName, String lastName) {
    this.consumerId = consumerId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public long getConsumerId() {
    return consumerId;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }
}
