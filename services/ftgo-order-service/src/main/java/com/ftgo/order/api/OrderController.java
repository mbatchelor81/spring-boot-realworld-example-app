package com.ftgo.order.api;

import com.ftgo.order.domain.OrderService;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Map<String, Object>> createOrder() {
    Long orderId = orderService.createOrder(1L);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("orderId", orderId, "status", "CREATED"));
  }

  @GetMapping("/{orderId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long orderId) {
    return ResponseEntity.ok(Map.of("orderId", orderId));
  }

  @DeleteMapping("/{orderId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
    orderService.cancelOrder(orderId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{orderId}/accept")
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public ResponseEntity<Void> acceptOrder(@PathVariable Long orderId) {
    orderService.acceptOrder(orderId, LocalDateTime.now().plusHours(1));
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{orderId}/preparing")
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public ResponseEntity<Void> noteOrderPreparing(@PathVariable Long orderId) {
    orderService.noteOrderPreparing(orderId);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{orderId}/ready")
  @PreAuthorize("hasRole('RESTAURANT_OWNER')")
  public ResponseEntity<Void> noteReadyForPickup(@PathVariable Long orderId) {
    orderService.noteOrderReadyForPickup(orderId);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{orderId}/picked-up")
  @PreAuthorize("hasRole('COURIER')")
  public ResponseEntity<Void> notePickedUp(@PathVariable Long orderId) {
    orderService.noteOrderPickedUp(orderId);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{orderId}/delivered")
  @PreAuthorize("hasRole('COURIER')")
  public ResponseEntity<Void> noteDelivered(@PathVariable Long orderId) {
    orderService.noteOrderDelivered(orderId);
    return ResponseEntity.ok().build();
  }
}
