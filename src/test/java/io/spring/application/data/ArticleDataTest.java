package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class ArticleDataTest {

  @Test
  void constructor_setsAllFields() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("uid", "user", "bio", "image", false);
    ArticleData data =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java"),
            profile);

    assertEquals("id", data.getId());
    assertEquals("slug", data.getSlug());
    assertEquals("title", data.getTitle());
    assertEquals("desc", data.getDescription());
    assertEquals("body", data.getBody());
    assertTrue(data.isFavorited());
    assertEquals(5, data.getFavoritesCount());
    assertEquals(now, data.getCreatedAt());
    assertEquals(now, data.getUpdatedAt());
    assertEquals(1, data.getTagList().size());
    assertEquals("java", data.getTagList().get(0));
    assertEquals(profile, data.getProfileData());
  }

  @Test
  void getCursor_returnsUpdatedAtCursor() {
    DateTime updatedAt = new DateTime(2024, 1, 1, 0, 0);
    ArticleData data =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            updatedAt,
            Collections.emptyList(),
            null);

    assertNotNull(data.getCursor());
    assertEquals(String.valueOf(updatedAt.getMillis()), data.getCursor().toString());
  }

  @Test
  void equals_withSameData_returnsTrue() {
    DateTime now = new DateTime();
    ArticleData d1 =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);
    ArticleData d2 =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);

    assertEquals(d1, d2);
    assertEquals(d1.hashCode(), d2.hashCode());
  }

  @Test
  void equals_withDifferentData_returnsFalse() {
    DateTime now = new DateTime();
    ArticleData d1 =
        new ArticleData(
            "id1",
            "slug1",
            "title1",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);
    ArticleData d2 =
        new ArticleData(
            "id2",
            "slug2",
            "title2",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);

    assertNotEquals(d1, d2);
  }

  @Test
  void toString_containsFieldValues() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);

    String str = data.toString();
    assertTrue(str.contains("slug"));
    assertTrue(str.contains("title"));
  }

  @Test
  void setters_modifyFields() {
    DateTime now = new DateTime();
    ArticleData data =
        new ArticleData(
            "id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            null);

    data.setFavorited(true);
    data.setFavoritesCount(10);

    assertTrue(data.isFavorited());
    assertEquals(10, data.getFavoritesCount());
  }
}
