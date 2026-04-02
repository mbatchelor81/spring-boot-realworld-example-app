package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArticleFavoriteCountTest {

  @Test
  void constructor_setsAllFields() {
    ArticleFavoriteCount data = new ArticleFavoriteCount("article-id", 10);

    assertEquals("article-id", data.getId());
    assertEquals(10, data.getCount());
  }

  @Test
  void equals_withSameData_returnsTrue() {
    ArticleFavoriteCount d1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount d2 = new ArticleFavoriteCount("id", 5);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void equals_withDifferentData_returnsFalse() {
    ArticleFavoriteCount d1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount d2 = new ArticleFavoriteCount("id2", 10);

    assertNotEquals(d1, d2);
  }

  @Test
  void toString_containsFieldValues() {
    ArticleFavoriteCount data = new ArticleFavoriteCount("article-id", 42);

    String str = data.toString();
    assertTrue(str.contains("article-id"));
    assertTrue(str.contains("42"));
  }

  @Test
  void zeroCount_isValid() {
    ArticleFavoriteCount data = new ArticleFavoriteCount("id", 0);

    assertEquals(0, data.getCount());
  }
}
