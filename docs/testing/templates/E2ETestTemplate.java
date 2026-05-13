package com.ftgo.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Template: End-to-End test that verifies a critical user journey across services.
 *
 * <p>E2E tests run against live (containerized) service instances. They verify the
 * full request path through multiple services.
 *
 * <p>Instructions:
 * <ol>
 *   <li>Start required services via Docker Compose or Testcontainers</li>
 *   <li>Configure RestAssured.baseURI to point at the API gateway</li>
 *   <li>Test a complete user journey (e.g., create order → accept → deliver)</li>
 *   <li>Keep to 10-15 critical path scenarios maximum</li>
 * </ol>
 *
 * <p>Run with: {@code ./gradlew test -PincludeTags=e2e}
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderLifecycleE2ETest {

  // Store IDs created during the test flow
  // private static long createdOrderId;

  @BeforeAll
  static void setUp() {
    // RestAssured.baseURI = "http://localhost";
    // RestAssured.port = 8080;
    // RestAssured.basePath = "/api";
  }

  @Test
  @Order(1)
  void step1_createOrder_returns201() {
    // String requestBody = """
    //     {
    //       "consumerId": 1,
    //       "restaurantId": 1,
    //       "lineItems": [
    //         { "menuItemId": "item-1", "quantity": 2 }
    //       ]
    //     }
    //     """;
    //
    // createdOrderId = given()
    //     .contentType(ContentType.JSON)
    //     .body(requestBody)
    //     .when()
    //     .post("/orders")
    //     .then()
    //     .statusCode(201)
    //     .body("state", equalTo("APPROVED"))
    //     .extract()
    //     .path("orderId");
    //
    // assertThat(createdOrderId).isPositive();
  }

  @Test
  @Order(2)
  void step2_acceptOrder_transitionsToAccepted() {
    // given()
    //     .contentType(ContentType.JSON)
    //     .body("{ \"readyBy\": \"2025-12-31T23:59:59\" }")
    //     .when()
    //     .post("/orders/" + createdOrderId + "/accept")
    //     .then()
    //     .statusCode(200)
    //     .body("state", equalTo("ACCEPTED"));
  }

  @Test
  @Order(3)
  void step3_prepareOrder_transitionsToPreparing() {
    // given()
    //     .when()
    //     .post("/orders/" + createdOrderId + "/preparing")
    //     .then()
    //     .statusCode(200)
    //     .body("state", equalTo("PREPARING"));
  }

  @Test
  @Order(4)
  void step4_readyForPickup_transitionsToReadyForPickup() {
    // given()
    //     .when()
    //     .post("/orders/" + createdOrderId + "/ready")
    //     .then()
    //     .statusCode(200)
    //     .body("state", equalTo("READY_FOR_PICKUP"));
  }

  @Test
  @Order(5)
  void step5_getOrderDetails_returnsFullOrderData() {
    // given()
    //     .when()
    //     .get("/orders/" + createdOrderId)
    //     .then()
    //     .statusCode(200)
    //     .body("orderId", equalTo((int) createdOrderId))
    //     .body("lineItems", hasSize(1))
    //     .body("lineItems[0].quantity", equalTo(2));
  }
}
