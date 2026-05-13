package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

class RoleHierarchyTest {

  private RoleHierarchy roleHierarchy;

  @BeforeEach
  void setUp() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy(
        "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n"
            + "ROLE_ADMIN > ROLE_COURIER\n"
            + "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER\n"
            + "ROLE_COURIER > ROLE_CUSTOMER");
    roleHierarchy = hierarchy;
  }

  @Test
  void adminHasAllRoles() {
    Collection<? extends GrantedAuthority> reachable =
        roleHierarchy.getReachableGrantedAuthorities(AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    assertThat(reachable)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER", "ROLE_COURIER", "ROLE_CUSTOMER");
  }

  @Test
  void restaurantOwnerHasCustomerRole() {
    Collection<? extends GrantedAuthority> reachable =
        roleHierarchy.getReachableGrantedAuthorities(
            AuthorityUtils.createAuthorityList("ROLE_RESTAURANT_OWNER"));
    assertThat(reachable)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_RESTAURANT_OWNER", "ROLE_CUSTOMER")
        .doesNotContain("ROLE_ADMIN", "ROLE_COURIER");
  }

  @Test
  void courierHasCustomerRole() {
    Collection<? extends GrantedAuthority> reachable =
        roleHierarchy.getReachableGrantedAuthorities(
            AuthorityUtils.createAuthorityList("ROLE_COURIER"));
    assertThat(reachable)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_COURIER", "ROLE_CUSTOMER")
        .doesNotContain("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER");
  }

  @Test
  void customerHasOnlyCustomerRole() {
    Collection<? extends GrantedAuthority> reachable =
        roleHierarchy.getReachableGrantedAuthorities(
            AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));
    assertThat(reachable)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_CUSTOMER")
        .doesNotContain("ROLE_ADMIN", "ROLE_RESTAURANT_OWNER", "ROLE_COURIER");
  }
}
