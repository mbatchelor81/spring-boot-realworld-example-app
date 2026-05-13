package com.ftgo.security.authorization;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ftgo.security.jwt.JwtTokenProvider;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = RbacTestApplication.class,
    properties = {
      "ftgo.security.jwt.enabled=true",
      "ftgo.security.jwt.issuer-uri=http://localhost:9080/realms/ftgo"
    })
@AutoConfigureMockMvc
class RbacAuthorizationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private JwtTokenProvider tokenProvider;

  private String customerToken;
  private String restaurantOwnerToken;
  private String courierToken;
  private String adminToken;

  @BeforeEach
  void setUp() {
    customerToken =
        tokenProvider.createToken("customer-1", "customer", Collections.singletonList("CUSTOMER"));

    restaurantOwnerToken =
        tokenProvider.createToken(
            "owner-1", "restaurant_owner", Collections.singletonList("RESTAURANT_OWNER"));

    courierToken =
        tokenProvider.createToken("courier-1", "courier", Collections.singletonList("COURIER"));

    adminToken =
        tokenProvider.createToken("admin-1", "admin", Collections.singletonList("ADMIN"));
  }

  @Nested
  @DisplayName("Order Endpoints")
  class OrderEndpoints {

    @Test
    @DisplayName("CUSTOMER can create orders")
    void customerCanCreateOrder() throws Exception {
      mockMvc
          .perform(post("/api/orders").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("COURIER can create orders (inherits CUSTOMER via hierarchy)")
    void courierCanCreateOrder() throws Exception {
      mockMvc
          .perform(post("/api/orders").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER can create orders (inherits CUSTOMER via hierarchy)")
    void restaurantOwnerCanCreateOrder() throws Exception {
      mockMvc
          .perform(post("/api/orders").header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("ADMIN can create orders (via role hierarchy)")
    void adminCanCreateOrder() throws Exception {
      mockMvc
          .perform(post("/api/orders").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CUSTOMER can view orders")
    void customerCanViewOrder() throws Exception {
      mockMvc
          .perform(get("/api/orders/1").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER can accept orders")
    void restaurantOwnerCanAcceptOrder() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/accept").header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER cannot accept orders")
    void customerCannotAcceptOrder() throws Exception {
      mockMvc
          .perform(put("/api/orders/1/accept").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER can mark order as preparing")
    void restaurantOwnerCanMarkPreparing() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/preparing")
                  .header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER can mark order ready for pickup")
    void restaurantOwnerCanMarkReady() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/ready")
                  .header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("COURIER can mark order as picked up")
    void courierCanMarkPickedUp() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/picked-up").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER cannot mark order as picked up")
    void customerCannotMarkPickedUp() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/picked-up").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COURIER can mark order as delivered")
    void courierCanMarkDelivered() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/delivered").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated request returns 401")
    void unauthenticatedReturns401() throws Exception {
      mockMvc.perform(post("/api/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ADMIN can accept orders (via hierarchy)")
    void adminCanAcceptOrder() throws Exception {
      mockMvc
          .perform(put("/api/orders/1/accept").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN can mark order as picked up (via hierarchy)")
    void adminCanMarkPickedUp() throws Exception {
      mockMvc
          .perform(put("/api/orders/1/picked-up").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Consumer Endpoints")
  class ConsumerEndpoints {

    @Test
    @DisplayName("ADMIN can create consumers")
    void adminCanCreateConsumer() throws Exception {
      mockMvc
          .perform(post("/api/consumers").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CUSTOMER cannot create consumers")
    void customerCannotCreateConsumer() throws Exception {
      mockMvc
          .perform(post("/api/consumers").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER cannot create consumers")
    void restaurantOwnerCannotCreateConsumer() throws Exception {
      mockMvc
          .perform(post("/api/consumers").header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CUSTOMER can view consumer details")
    void customerCanViewConsumer() throws Exception {
      mockMvc
          .perform(get("/api/consumers/1").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN can list all consumers")
    void adminCanListConsumers() throws Exception {
      mockMvc
          .perform(get("/api/consumers").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER cannot list all consumers")
    void customerCannotListConsumers() throws Exception {
      mockMvc
          .perform(get("/api/consumers").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Restaurant Endpoints")
  class RestaurantEndpoints {

    @Test
    @DisplayName("RESTAURANT_OWNER can create restaurants")
    void restaurantOwnerCanCreateRestaurant() throws Exception {
      mockMvc
          .perform(
              post("/api/restaurants").header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CUSTOMER cannot create restaurants")
    void customerCannotCreateRestaurant() throws Exception {
      mockMvc
          .perform(post("/api/restaurants").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN can create restaurants (via hierarchy)")
    void adminCanCreateRestaurant() throws Exception {
      mockMvc
          .perform(post("/api/restaurants").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CUSTOMER can view restaurants")
    void customerCanViewRestaurant() throws Exception {
      mockMvc
          .perform(get("/api/restaurants/1").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER can revise menu")
    void restaurantOwnerCanReviseMenu() throws Exception {
      mockMvc
          .perform(
              put("/api/restaurants/1/menu")
                  .header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER cannot revise menu")
    void customerCannotReviseMenu() throws Exception {
      mockMvc
          .perform(
              put("/api/restaurants/1/menu").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Courier Endpoints")
  class CourierEndpoints {

    @Test
    @DisplayName("ADMIN can create couriers")
    void adminCanCreateCourier() throws Exception {
      mockMvc
          .perform(post("/api/couriers").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("CUSTOMER cannot create couriers")
    void customerCannotCreateCourier() throws Exception {
      mockMvc
          .perform(post("/api/couriers").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COURIER cannot create couriers")
    void courierCannotCreateCourier() throws Exception {
      mockMvc
          .perform(post("/api/couriers").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COURIER can plan delivery")
    void courierCanPlanDelivery() throws Exception {
      mockMvc
          .perform(
              put("/api/couriers/1/delivery").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER cannot plan delivery")
    void customerCannotPlanDelivery() throws Exception {
      mockMvc
          .perform(
              put("/api/couriers/1/delivery").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("COURIER can update availability")
    void courierCanUpdateAvailability() throws Exception {
      mockMvc
          .perform(
              put("/api/couriers/1/availability").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CUSTOMER can view courier details")
    void customerCanViewCourier() throws Exception {
      mockMvc
          .perform(get("/api/couriers/1").header("Authorization", "Bearer " + customerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN can plan delivery (via hierarchy)")
    void adminCanPlanDelivery() throws Exception {
      mockMvc
          .perform(
              put("/api/couriers/1/delivery").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Role Hierarchy")
  class RoleHierarchyTests {

    @Test
    @DisplayName("ADMIN inherits CUSTOMER permissions")
    void adminInheritsCustomerPermissions() throws Exception {
      mockMvc
          .perform(get("/api/orders/1").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN inherits RESTAURANT_OWNER permissions")
    void adminInheritsRestaurantOwnerPermissions() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/accept").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN inherits COURIER permissions")
    void adminInheritsCourierPermissions() throws Exception {
      mockMvc
          .perform(
              put("/api/orders/1/picked-up").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RESTAURANT_OWNER inherits CUSTOMER permissions")
    void restaurantOwnerInheritsCustomerPermissions() throws Exception {
      mockMvc
          .perform(
              get("/api/orders/1").header("Authorization", "Bearer " + restaurantOwnerToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("COURIER inherits CUSTOMER permissions")
    void courierInheritsCustomerPermissions() throws Exception {
      mockMvc
          .perform(get("/api/orders/1").header("Authorization", "Bearer " + courierToken))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("Resource Ownership")
  class ResourceOwnershipTests {

    @Test
    @DisplayName("Owner can cancel their own order")
    void ownerCanCancelOwnOrder() throws Exception {
      String ownerToken =
          tokenProvider.createToken(
              "owner-1", "owner-1", Collections.singletonList("CUSTOMER"));
      mockMvc
          .perform(delete("/api/orders/1").header("Authorization", "Bearer " + ownerToken))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ADMIN can cancel any order")
    void adminCanCancelAnyOrder() throws Exception {
      mockMvc
          .perform(delete("/api/orders/1").header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isNoContent());
    }
  }
}
