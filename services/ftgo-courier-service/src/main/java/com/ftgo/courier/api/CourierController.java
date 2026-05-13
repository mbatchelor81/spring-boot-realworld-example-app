package com.ftgo.courier.api;

import com.ftgo.courier.domain.CourierService;
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
@RequestMapping("/api/couriers")
public class CourierController {

  private final CourierService courierService;

  public CourierController(CourierService courierService) {
    this.courierService = courierService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> createCourier() {
    Long courierId = courierService.createCourier("New", "Courier");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("courierId", courierId, "status", "CREATED"));
  }

  @GetMapping("/{courierId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Map<String, Object>> getCourier(@PathVariable Long courierId) {
    return ResponseEntity.ok(Map.of("courierId", courierId));
  }

  @PutMapping("/{courierId}/delivery")
  @PreAuthorize("hasRole('COURIER')")
  public ResponseEntity<Void> planDelivery(@PathVariable Long courierId) {
    courierService.planDelivery(courierId, 1L);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{courierId}/availability")
  @PreAuthorize("hasRole('COURIER')")
  public ResponseEntity<Void> updateAvailability(@PathVariable Long courierId) {
    return ResponseEntity.ok().build();
  }
}
