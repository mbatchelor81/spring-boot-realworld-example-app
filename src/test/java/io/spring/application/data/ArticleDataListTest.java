package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class ArticleDataListTest {

  @Test
  void constructor_setsAllFields() {
    DateTime now = new DateTime();
    ArticleData article =
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
    ArticleDataList list = new ArticleDataList(Arrays.asList(article), 1);

    assertEquals(1, list.getArticleDatas().size());
    assertEquals(1, list.getCount());
  }

  @Test
  void constructor_withEmptyList_setsEmptyList() {
    ArticleDataList list = new ArticleDataList(Collections.emptyList(), 0);

    assertTrue(list.getArticleDatas().isEmpty());
    assertEquals(0, list.getCount());
  }

  @Test
  void constructor_withMultipleArticles_setsCorrectCount() {
    DateTime now = new DateTime();
    ArticleData a1 =
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
    ArticleData a2 =
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
    ArticleDataList list = new ArticleDataList(Arrays.asList(a1, a2), 5);

    assertEquals(2, list.getArticleDatas().size());
    assertEquals(5, list.getCount());
  }
}
