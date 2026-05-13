package com.ftgo.consumer.domain;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

  @PreAuthorize("hasRole('ADMIN')")
  public Long createConsumer(String firstName, String lastName) {
    return 1L;
  }

  @PreAuthorize("hasRole('CUSTOMER')")
  public Object getConsumer(Long consumerId) {
    return null;
  }

  @PreAuthorize("hasRole('ADMIN')")
  public Object getAllConsumers() {
    return null;
  }
}
