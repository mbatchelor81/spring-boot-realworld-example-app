package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProfileDataTest {

  @Test
  void constructor_setsAllFields() {
    ProfileData data = new ProfileData("id", "user", "bio", "image", true);

    assertEquals("id", data.getId());
    assertEquals("user", data.getUsername());
    assertEquals("bio", data.getBio());
    assertEquals("image", data.getImage());
    assertTrue(data.isFollowing());
  }

  @Test
  void equals_withSameData_returnsTrue() {
    ProfileData d1 = new ProfileData("id", "user", "bio", "image", true);
    ProfileData d2 = new ProfileData("id", "user", "bio", "image", true);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void equals_withDifferentData_returnsFalse() {
    ProfileData d1 = new ProfileData("id1", "user1", "bio", "image", true);
    ProfileData d2 = new ProfileData("id2", "user2", "bio", "image", false);

    assertNotEquals(d1, d2);
  }

  @Test
  void toString_containsFieldValues() {
    ProfileData data = new ProfileData("id", "testuser", "mybio", "myimage", true);

    String str = data.toString();
    assertTrue(str.contains("testuser"));
    assertTrue(str.contains("mybio"));
  }

  @Test
  void setters_modifyFields() {
    ProfileData data = new ProfileData("id", "user", "bio", "image", false);

    data.setFollowing(true);
    data.setUsername("newuser");
    data.setBio("newbio");
    data.setImage("newimage");

    assertTrue(data.isFollowing());
    assertEquals("newuser", data.getUsername());
    assertEquals("newbio", data.getBio());
    assertEquals("newimage", data.getImage());
  }
}
