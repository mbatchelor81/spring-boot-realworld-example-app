package com.ftgo.restaurant.domain;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class RestaurantService {

  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public Long createRestaurant(String name) {
    return 1L;
  }

  @PreAuthorize("hasRole('CUSTOMER')")
  public Object getRestaurant(Long restaurantId) {
    return null;
  }

  // TODO: Add ownership validation when database layer is implemented.
  // Pattern: look up restaurant's ownerId from DB, then use
  // @resourceOwnershipEvaluator.isOwnerOrAdmin(authentication, #ownerFromDb)
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public void reviseMenu(Long restaurantId) {
    // revise menu logic
  }
}
