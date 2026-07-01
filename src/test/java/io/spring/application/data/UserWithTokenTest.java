package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserWithTokenTest {

  @Test
  void constructor_setsAllFields() {
    UserData userData = new UserData("id", "test@example.com", "user", "bio", "image");
    UserWithToken uwt = new UserWithToken(userData, "jwt-token");

    assertEquals("test@example.com", uwt.getEmail());
    assertEquals("user", uwt.getUsername());
    assertEquals("bio", uwt.getBio());
    assertEquals("image", uwt.getImage());
    assertEquals("jwt-token", uwt.getToken());
  }

  @Test
  void constructor_withEmptyToken_setsEmptyToken() {
    UserData userData = new UserData("id", "test@example.com", "user", "bio", "image");
    UserWithToken uwt = new UserWithToken(userData, "");

    assertEquals("", uwt.getToken());
  }

  @Test
  void constructor_withNullBioAndImage_setsNull() {
    UserData userData = new UserData("id", "test@example.com", "user", null, null);
    UserWithToken uwt = new UserWithToken(userData, "token");

    assertNull(uwt.getBio());
    assertNull(uwt.getImage());
  }
}
