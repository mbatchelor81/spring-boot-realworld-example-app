package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class FtgoPermissionEvaluatorTest {

  private FtgoPermissionEvaluator evaluator;

  @BeforeEach
  void setUp() {
    evaluator = new FtgoPermissionEvaluator();
  }

  @Test
  void hasPermission_withMatchingAuthority_returnsTrue() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ORDER_CREATE")));
    assertThat(evaluator.hasPermission(auth, null, "ORDER_CREATE")).isTrue();
  }

  @Test
  void hasPermission_withoutMatchingAuthority_returnsFalse() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ORDER_READ")));
    assertThat(evaluator.hasPermission(auth, null, "ORDER_CREATE")).isFalse();
  }

  @Test
  void hasPermission_withNullAuthentication_returnsFalse() {
    assertThat(evaluator.hasPermission(null, null, "ORDER_CREATE")).isFalse();
  }

  @Test
  void hasPermission_withNullPermission_returnsFalse() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken("user", null, Collections.emptyList());
    assertThat(evaluator.hasPermission(auth, null, null)).isFalse();
  }

  @Test
  void hasPermission_withIdAndType_matchingAuthority_returnsTrue() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ORDER_READ")));
    assertThat(evaluator.hasPermission(auth, 1L, "Order", "ORDER_READ")).isTrue();
  }

  @Test
  void hasPermission_withIdAndType_noMatchingAuthority_returnsFalse() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority("ORDER_READ")));
    assertThat(evaluator.hasPermission(auth, 1L, "Order", "ORDER_CREATE")).isFalse();
  }
}
