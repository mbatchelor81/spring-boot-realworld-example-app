package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FollowRelationTest {

  @Test
  void constructor_shouldSetUserIdAndTargetId() {
    FollowRelation relation = new FollowRelation("user1", "user2");

    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  void noArgConstructor_shouldCreateInstanceWithNullFields() {
    FollowRelation relation = new FollowRelation();

    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  void setters_shouldUpdateFields() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("follower");
    relation.setTargetId("followed");

    assertEquals("follower", relation.getUserId());
    assertEquals("followed", relation.getTargetId());
  }

  @Test
  void equals_shouldBeEqualForSameValues() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user2");

    assertEquals(r1, r2);
  }

  @Test
  void equals_shouldNotBeEqualForDifferentValues() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user3");

    assertNotEquals(r1, r2);
  }

  @Test
  void hashCode_shouldBeEqualForEqualObjects() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user2");

    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void toString_shouldContainFields() {
    FollowRelation relation = new FollowRelation("user1", "user2");

    String str = relation.toString();
    assertTrue(str.contains("user1"));
    assertTrue(str.contains("user2"));
  }
}
