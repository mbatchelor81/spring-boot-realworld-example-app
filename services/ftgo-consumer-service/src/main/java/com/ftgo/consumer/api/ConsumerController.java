package com.ftgo.consumer.api;

import com.ftgo.consumer.domain.ConsumerService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

  private final ConsumerService consumerService;

  public ConsumerController(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> createConsumer() {
    Long consumerId = consumerService.createConsumer("New", "Consumer");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Map.of("consumerId", consumerId, "status", "CREATED"));
  }

  @GetMapping("/{consumerId}")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<Map<String, Object>> getConsumer(@PathVariable Long consumerId) {
    return ResponseEntity.ok(Map.of("consumerId", consumerId));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> getAllConsumers() {
    return ResponseEntity.ok(Map.of("consumers", java.util.List.of()));
  }
}
