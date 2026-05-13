package com.ftgo.restaurant.api;

import com.ftgo.restaurant.domain.RestaurantService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

  private final RestaurantService restaurantService;

  public RestaurantController(RestaurantService restaurantService) {
    this.restaurantService = restaurantService;
  }

  @PostMapping
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public ResponseEntity<Map<String, Object>> createRestaurant() {
    Long restaurantId = restaurantService.createRestaurant("New Restaurant");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("restaurantId", restaurantId, "status", "CREATED"));
  }

  @GetMapping("/{restaurantId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Map<String, Object>> getRestaurant(@PathVariable Long restaurantId) {
    return ResponseEntity.ok(Map.of("restaurantId", restaurantId));
  }

  @PutMapping("/{restaurantId}/menu")
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public ResponseEntity<Void> reviseMenu(@PathVariable Long restaurantId) {
    return ResponseEntity.ok().build();
  }
}
