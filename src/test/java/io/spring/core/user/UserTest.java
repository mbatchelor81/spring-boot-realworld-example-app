package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void constructor_shouldCreateUserWithAllFields() {
    User user = new User("test@example.com", "testuser", "password123", "my bio", "image.jpg");

    assertNotNull(user.getId());
    assertEquals("test@example.com", user.getEmail());
    assertEquals("testuser", user.getUsername());
    assertEquals("password123", user.getPassword());
    assertEquals("my bio", user.getBio());
    assertEquals("image.jpg", user.getImage());
  }

  @Test
  void constructor_shouldGenerateUniqueIds() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("c@d.com", "user2", "pass", "", "");

    assertNotEquals(user1.getId(), user2.getId());
  }

  @Test
  void update_shouldUpdateAllFieldsWhenAllProvided() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "old.jpg");

    user.update("new@example.com", "newuser", "newpass", "new bio", "new.jpg");

    assertEquals("new@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
    assertEquals("newpass", user.getPassword());
    assertEquals("new bio", user.getBio());
    assertEquals("new.jpg", user.getImage());
  }

  @Test
  void update_shouldNotUpdateEmailWhenNull() {
    User user = new User("original@example.com", "user", "pass", "bio", "img");

    user.update(null, "newuser", "newpass", "new bio", "new.jpg");

    assertEquals("original@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
  }

  @Test
  void update_shouldNotUpdateEmailWhenEmpty() {
    User user = new User("original@example.com", "user", "pass", "bio", "img");

    user.update("", "newuser", "newpass", "new bio", "new.jpg");

    assertEquals("original@example.com", user.getEmail());
    assertEquals("newuser", user.getUsername());
  }

  @Test
  void update_shouldNotUpdateUsernameWhenNull() {
    User user = new User("email@example.com", "originaluser", "pass", "bio", "img");

    user.update("new@example.com", null, "newpass", "new bio", "new.jpg");

    assertEquals("originaluser", user.getUsername());
    assertEquals("new@example.com", user.getEmail());
  }

  @Test
  void update_shouldNotUpdateUsernameWhenEmpty() {
    User user = new User("email@example.com", "originaluser", "pass", "bio", "img");

    user.update("new@example.com", "", "newpass", "new bio", "new.jpg");

    assertEquals("originaluser", user.getUsername());
  }

  @Test
  void update_shouldNotUpdatePasswordWhenNull() {
    User user = new User("email@example.com", "user", "originalpass", "bio", "img");

    user.update("new@example.com", "newuser", null, "new bio", "new.jpg");

    assertEquals("originalpass", user.getPassword());
  }

  @Test
  void update_shouldNotUpdatePasswordWhenEmpty() {
    User user = new User("email@example.com", "user", "originalpass", "bio", "img");

    user.update("new@example.com", "newuser", "", "new bio", "new.jpg");

    assertEquals("originalpass", user.getPassword());
  }

  @Test
  void update_shouldNotUpdateBioWhenNull() {
    User user = new User("email@example.com", "user", "pass", "original bio", "img");

    user.update("new@example.com", "newuser", "newpass", null, "new.jpg");

    assertEquals("original bio", user.getBio());
  }

  @Test
  void update_shouldNotUpdateBioWhenEmpty() {
    User user = new User("email@example.com", "user", "pass", "original bio", "img");

    user.update("new@example.com", "newuser", "newpass", "", "new.jpg");

    assertEquals("original bio", user.getBio());
  }

  @Test
  void update_shouldNotUpdateImageWhenNull() {
    User user = new User("email@example.com", "user", "pass", "bio", "original.jpg");

    user.update("new@example.com", "newuser", "newpass", "new bio", null);

    assertEquals("original.jpg", user.getImage());
  }

  @Test
  void update_shouldNotUpdateImageWhenEmpty() {
    User user = new User("email@example.com", "user", "pass", "bio", "original.jpg");

    user.update("new@example.com", "newuser", "newpass", "new bio", "");

    assertEquals("original.jpg", user.getImage());
  }

  @Test
  void update_shouldNotChangeAnyFieldWhenAllNulls() {
    User user = new User("email@example.com", "user", "pass", "bio", "img");

    user.update(null, null, null, null, null);

    assertEquals("email@example.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void update_shouldNotChangeAnyFieldWhenAllEmpty() {
    User user = new User("email@example.com", "user", "pass", "bio", "img");

    user.update("", "", "", "", "");

    assertEquals("email@example.com", user.getEmail());
    assertEquals("user", user.getUsername());
    assertEquals("pass", user.getPassword());
    assertEquals("bio", user.getBio());
    assertEquals("img", user.getImage());
  }

  @Test
  void equals_shouldBeEqualWhenSameId() {
    User user = new User("a@b.com", "user1", "pass", "", "");

    assertEquals(user, user);
  }

  @Test
  void equals_shouldNotBeEqualForDifferentUsers() {
    User user1 = new User("a@b.com", "user1", "pass", "", "");
    User user2 = new User("a@b.com", "user1", "pass", "", "");

    assertNotEquals(user1, user2);
  }

  @Test
  void equals_shouldNotBeEqualToNull() {
    User user = new User("a@b.com", "user1", "pass", "", "");

    assertNotEquals(null, user);
  }

  @Test
  void hashCode_shouldBeConsistent() {
    User user = new User("a@b.com", "user1", "pass", "", "");
    int hash1 = user.hashCode();
    int hash2 = user.hashCode();

    assertEquals(hash1, hash2);
  }
}
