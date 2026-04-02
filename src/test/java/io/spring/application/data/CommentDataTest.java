package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CommentDataTest {

  @Test
  void constructor_setsAllFields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "image", false);
    CommentData data = new CommentData("id", "body", "article-id", now, now, profile);

    assertEquals("id", data.getId());
    assertEquals("body", data.getBody());
    assertEquals("article-id", data.getArticleId());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void getCursor_returnsCreatedAtCursor() {
    DateTime createdAt = new DateTime(2024, 6, 1, 12, 0);
    CommentData data = new CommentData("id", "body", "article-id", createdAt, new DateTime(), null);

    assertNotNull(data.getCursor());
    assertEquals(String.valueOf(createdAt.getMillis()), data.getCursor().toString());
  }

  @Test
  void equals_withSameData_returnsTrue() {
    DateTime now = new DateTime();
    CommentData d1 = new CommentData("id", "body", "article-id", now, now, null);
    CommentData d2 = new CommentData("id", "body", "article-id", now, now, null);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void equals_withDifferentData_returnsFalse() {
    DateTime now = new DateTime();
    CommentData d1 = new CommentData("id1", "body1", "article-id", now, now, null);
    CommentData d2 = new CommentData("id2", "body2", "article-id", now, now, null);

    assertNotEquals(d1, d2);
  }

  @Test
  void toString_containsFieldValues() {
    DateTime now = new DateTime();
    CommentData data = new CommentData("id", "body", "article-id", now, now, null);

    String str = data.toString();
    assertTrue(str.contains("id"));
    assertTrue(str.contains("body"));
  }
}
