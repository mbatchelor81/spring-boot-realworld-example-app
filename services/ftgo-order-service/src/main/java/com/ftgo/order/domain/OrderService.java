package com.ftgo.order.domain;

import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  @PreAuthorize("hasRole('CUSTOMER')")
  public Long createOrder(Long restaurantId) {
    return 1L;
  }

  // TODO: Add ownership validation when database layer is implemented.
  // Pattern: look up order's consumerId from DB, then use
  // @resourceOwnershipEvaluator.isOwnerOrAdmin(authentication, #ownerFromDb)
  @PreAuthorize("hasRole('CUSTOMER')")
  public void cancelOrder(Long orderId) {
    // cancel order logic
  }

  // TODO: Add ownership validation when database layer is implemented.
  @PreAuthorize("hasRole('CUSTOMER')")
  public void reviseOrder(Long orderId) {
    // revise order logic
  }

  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public void acceptOrder(Long orderId, LocalDateTime readyBy) {
    // accept order logic
  }

  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public void noteOrderPreparing(Long orderId) {
    // note preparing logic
  }

  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public void noteOrderReadyForPickup(Long orderId) {
    // note ready for pickup
  }

  @PreAuthorize("hasRole('COURIER')")
  public void noteOrderPickedUp(Long orderId) {
    // note picked up
  }

  @PreAuthorize("hasRole('COURIER')")
  public void noteOrderDelivered(Long orderId) {
    // note delivered
  }

  @PreAuthorize("hasRole('CUSTOMER')")
  public Object getOrder(Long orderId) {
    return null;
  }
}
