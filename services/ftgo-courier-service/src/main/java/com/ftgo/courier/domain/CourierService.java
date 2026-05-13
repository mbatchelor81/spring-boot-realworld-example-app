package com.ftgo.courier.domain;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class CourierService {

  @PreAuthorize("hasRole('ADMIN')")
  public Long createCourier(String firstName, String lastName) {
    return 1L;
  }

  @PreAuthorize("hasRole('COURIER')")
  public void planDelivery(Long courierId, Long orderId) {
    // plan delivery logic
  }

  // TODO: Add ownership validation when database layer is implemented.
  // Pattern: look up courier's ownerId from DB, then use
  // @resourceOwnershipEvaluator.isOwnerOrAdmin(authentication, #ownerFromDb)
  @PreAuthorize("hasRole('COURIER')")
  public void updateAvailability(Long courierId, boolean available) {
    // update availability logic
  }

  @PreAuthorize("hasRole('CUSTOMER')")
  public Object getCourier(Long courierId) {
    return null;
  }
}
