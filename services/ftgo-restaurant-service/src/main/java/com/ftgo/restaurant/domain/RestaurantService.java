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

  @PreAuthorize(
      "hasRole('RESTAURANT_OWNER') and @resourceOwnershipEvaluator.isOwnerOrAdmin("
          + "authentication, #ownerId.toString())")
  public void reviseMenu(Long restaurantId, String ownerId) {
    // revise menu logic
  }
}
