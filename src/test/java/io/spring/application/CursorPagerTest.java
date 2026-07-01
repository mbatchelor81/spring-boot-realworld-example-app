package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPagerTest {

  private ArticleData createArticleData(DateTime updatedAt) {
    return new ArticleData(
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
        new ProfileData("uid", "user", "", "", false));
  }

  @Test
  void constructor_withNextDirectionAndHasExtra_setsNextTrue() {
    ArticleData a1 = createArticleData(new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, true);

    assertTrue(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void constructor_withNextDirectionAndNoExtra_setsNextFalse() {
    ArticleData a1 = createArticleData(new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void constructor_withPrevDirectionAndHasExtra_setsPreviousTrue() {
    ArticleData a1 = createArticleData(new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, true);

    assertFalse(pager.hasNext());
    assertTrue(pager.hasPrevious());
  }

  @Test
  void constructor_withPrevDirectionAndNoExtra_setsPreviousFalse() {
    ArticleData a1 = createArticleData(new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.PREV, false);

    assertFalse(pager.hasNext());
    assertFalse(pager.hasPrevious());
  }

  @Test
  void getStartCursor_withData_returnsFirstElementCursor() {
    DateTime dt = new DateTime(2024, 1, 1, 0, 0);
    ArticleData a1 = createArticleData(dt);
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);

    assertNotNull(pager.getStartCursor());
    assertEquals(String.valueOf(dt.getMillis()), pager.getStartCursor().toString());
  }

  @Test
  void getStartCursor_withEmptyData_returnsNull() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getStartCursor());
  }

  @Test
  void getEndCursor_withData_returnsLastElementCursor() {
    DateTime dt1 = new DateTime(2024, 1, 1, 0, 0);
    DateTime dt2 = new DateTime(2024, 1, 2, 0, 0);
    ArticleData a1 = createArticleData(dt1);
    ArticleData a2 = createArticleData(dt2);
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(a1, a2), Direction.NEXT, false);

    assertNotNull(pager.getEndCursor());
    assertEquals(String.valueOf(dt2.getMillis()), pager.getEndCursor().toString());
  }

  @Test
  void getEndCursor_withEmptyData_returnsNull() {
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    assertNull(pager.getEndCursor());
  }

  @Test
  void getData_returnsProvidedData() {
    ArticleData a1 = createArticleData(new DateTime());
    CursorPager<ArticleData> pager = new CursorPager<>(Arrays.asList(a1), Direction.NEXT, false);

    assertEquals(1, pager.getData().size());
    assertEquals(a1, pager.getData().get(0));
  }
}
