package com.ftgo.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class ResourceOwnershipEvaluatorTest {

  private ResourceOwnershipEvaluator evaluator;

  @BeforeEach
  void setUp() {
    evaluator = new ResourceOwnershipEvaluator();
  }

  @Test
  void isAdmin_withAdminRole_returnsTrue() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "admin", null, List.of(new SimpleGrantedAuthority(FtgoRole.ADMIN)));
    assertThat(evaluator.isAdmin(auth)).isTrue();
  }

  @Test
  void isAdmin_withNonAdminRole_returnsFalse() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority(FtgoRole.CUSTOMER)));
    assertThat(evaluator.isAdmin(auth)).isFalse();
  }

  @Test
  void isAdmin_withNullAuthentication_returnsFalse() {
    assertThat(evaluator.isAdmin(null)).isFalse();
  }

  @Test
  void isOwnerOrAdmin_withAdmin_returnsTrue() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "admin", null, List.of(new SimpleGrantedAuthority(FtgoRole.ADMIN)));
    assertThat(evaluator.isOwnerOrAdmin(auth, "some-other-user")).isTrue();
  }

  @Test
  void isOwnerOrAdmin_withNullAuthentication_returnsFalse() {
    assertThat(evaluator.isOwnerOrAdmin(null, "owner")).isFalse();
  }

  @Test
  void isOwner_withNullAuthentication_returnsFalse() {
    assertThat(evaluator.isOwner(null, "owner")).isFalse();
  }

  @Test
  void isOwner_withNullResourceOwnerId_returnsFalse() {
    TestingAuthenticationToken auth =
        new TestingAuthenticationToken(
            "user", null, List.of(new SimpleGrantedAuthority(FtgoRole.CUSTOMER)));
    assertThat(evaluator.isOwner(auth, null)).isFalse();
  }
}
