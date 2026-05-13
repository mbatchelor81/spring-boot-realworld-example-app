package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserContext;
import com.ftgo.security.jwt.JwtTokenProvider;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableAutoConfiguration
public class RbacTestApplication {

  private static final String TEST_ISSUER = "http://localhost:9080/realms/ftgo";

  @Bean
  public JwtTokenProvider jwtTokenProvider() {
    return JwtTokenProvider.withGeneratedKey(TEST_ISSUER, 3600);
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtTokenProvider tokenProvider) {
    try {
      RSAPublicKey publicKey = tokenProvider.getRsaKey().toRSAPublicKey();
      NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
      decoder.setJwtValidator(new JwtTimestampValidator());
      return decoder;
    } catch (com.nimbusds.jose.JOSEException e) {
      throw new IllegalStateException("Failed to build JwtDecoder", e);
    }
  }

  @RestController
  static class OrderTestController {

    @PostMapping("/api/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> createOrder() {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("orderId", 1, "status", "CREATED"));
    }

    @GetMapping("/api/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long orderId) {
      return ResponseEntity.ok(Map.of("orderId", orderId));
    }

    @DeleteMapping("/api/orders/{orderId}")
    @PreAuthorize(
        "hasRole('CUSTOMER') and @resourceOwnershipEvaluator.isOwnerOrAdmin("
            + "authentication, #ownerId)")
    public ResponseEntity<Void> cancelOrder(
        @PathVariable Long orderId, @RequestParam String ownerId) {
      return ResponseEntity.noContent().build();
    }

    @PutMapping("/api/orders/{orderId}/accept")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> acceptOrder(@PathVariable Long orderId) {
      return ResponseEntity.ok().build();
    }

    @PutMapping("/api/orders/{orderId}/preparing")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> noteOrderPreparing(@PathVariable Long orderId) {
      return ResponseEntity.ok().build();
    }

    @PutMapping("/api/orders/{orderId}/ready")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> noteReadyForPickup(@PathVariable Long orderId) {
      return ResponseEntity.ok().build();
    }

    @PutMapping("/api/orders/{orderId}/picked-up")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Void> notePickedUp(@PathVariable Long orderId) {
      return ResponseEntity.ok().build();
    }

    @PutMapping("/api/orders/{orderId}/delivered")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Void> noteDelivered(@PathVariable Long orderId) {
      return ResponseEntity.ok().build();
    }
  }

  @RestController
  static class ConsumerTestController {

    @PostMapping("/api/consumers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createConsumer() {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("consumerId", 1, "status", "CREATED"));
    }

    @GetMapping("/api/consumers/{consumerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getConsumer(@PathVariable Long consumerId) {
      return ResponseEntity.ok(Map.of("consumerId", consumerId));
    }

    @GetMapping("/api/consumers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllConsumers() {
      return ResponseEntity.ok(Map.of("consumers", java.util.List.of()));
    }
  }

  @RestController
  static class RestaurantTestController {

    @PostMapping("/api/restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Map<String, Object>> createRestaurant() {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("restaurantId", 1, "status", "CREATED"));
    }

    @GetMapping("/api/restaurants/{restaurantId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getRestaurant(@PathVariable Long restaurantId) {
      return ResponseEntity.ok(Map.of("restaurantId", restaurantId));
    }

    @PutMapping("/api/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public ResponseEntity<Void> reviseMenu(@PathVariable Long restaurantId) {
      return ResponseEntity.ok().build();
    }
  }

  @RestController
  static class CourierTestController {

    @PostMapping("/api/couriers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createCourier() {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(Map.of("courierId", 1, "status", "CREATED"));
    }

    @GetMapping("/api/couriers/{courierId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getCourier(@PathVariable Long courierId) {
      return ResponseEntity.ok(Map.of("courierId", courierId));
    }

    @PutMapping("/api/couriers/{courierId}/delivery")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Void> planDelivery(@PathVariable Long courierId) {
      return ResponseEntity.ok().build();
    }

    @PutMapping("/api/couriers/{courierId}/availability")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Void> updateAvailability(@PathVariable Long courierId) {
      return ResponseEntity.ok().build();
    }
  }
}
