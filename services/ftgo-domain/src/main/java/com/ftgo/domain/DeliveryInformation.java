package com.ftgo.domain;

import com.ftgo.common.Address;
import java.time.LocalDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
@Access(AccessType.FIELD)
public class DeliveryInformation {

  private LocalDateTime deliveryTime;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "street1",
        column = @Column(name = "delivery_address_street1")),
    @AttributeOverride(
        name = "street2",
        column = @Column(name = "delivery_address_street2")),
    @AttributeOverride(name = "city", column = @Column(name = "delivery_address_city")),
    @AttributeOverride(name = "state", column = @Column(name = "delivery_address_state")),
    @AttributeOverride(name = "zip", column = @Column(name = "delivery_address_zip")),
  })
  private Address deliveryAddress;
}
