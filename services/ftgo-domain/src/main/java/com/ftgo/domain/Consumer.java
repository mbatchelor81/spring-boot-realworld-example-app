package com.ftgo.domain;

import com.ftgo.common.Money;
import com.ftgo.common.PersonName;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "consumers")
@Access(AccessType.FIELD)
@DynamicUpdate
public class Consumer {

  @Id @GeneratedValue private Long id;

  @Embedded private PersonName name;

  private Consumer() {}

  public Consumer(PersonName name) {
    this.name = name;
  }

  public void validateOrderByConsumer(Money orderTotal) {
    // implement some business logic
  }

  public Long getId() {
    return id;
  }

  public PersonName getName() {
    return name;
  }
}
