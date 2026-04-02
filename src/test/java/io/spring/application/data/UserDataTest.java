package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserDataTest {

  @Test
  void constructor_setsAllFields() {
    UserData data = new UserData("id", "test@example.com", "user", "bio", "image");

    assertEquals("id", data.getId());
    assertEquals("test@example.com", data.getEmail());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
  }

  @Test
  void equals_withSameData_returnsTrue() {
    UserData d1 = new UserData("id", "test@example.com", "user", "bio", "image");
    UserData d2 = new UserData("id", "test@example.com", "user", "bio", "image");

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void equals_withDifferentData_returnsFalse() {
    UserData d1 = new UserData("id1", "a@example.com", "user1", "bio", "image");
    UserData d2 = new UserData("id2", "b@example.com", "user2", "bio", "image");

    assertNotEquals(d1, d2);
  }

  @Test
  void toString_containsFieldValues() {
    UserData data = new UserData("id", "test@example.com", "user", "bio", "image");

    String str = data.toString();
    assertTrue(str.contains("test@example.com"));
    assertTrue(str.contains("user"));
  }

  @Test
  void setters_modifyFields() {
    UserData data = new UserData("id", "test@example.com", "user", "bio", "image");

    data.setEmail("new@example.com");
    data.setUsername("newuser");
    data.setBio("new bio");
    data.setImage("new image");

    assertEquals("new@example.com", data.getEmail());
    assertEquals("newuser", data.getUsername());
    assertEquals("new bio", data.getBio());
    assertEquals("new image", data.getImage());
  }
}
