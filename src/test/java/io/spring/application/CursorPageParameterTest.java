package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.CursorPager.Direction;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CursorPageParameterTest {

  @Test
  void constructor_withValidLimit_setsLimit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 50, Direction.NEXT);

    assertEquals(50, param.getLimit());
  }

  @Test
  void constructor_withExceedingLimit_capsAtMaxLimit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 2000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void constructor_withZeroLimit_keepsDefault() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 0, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void constructor_withNegativeLimit_keepsDefault() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, -5, Direction.NEXT);

    assertEquals(20, param.getLimit());
  }

  @Test
  void isNext_withNextDirection_returnsTrue() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertTrue(param.isNext());
  }

  @Test
  void isNext_withPrevDirection_returnsFalse() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.PREV);

    assertFalse(param.isNext());
  }

  @Test
  void getQueryLimit_returnsLimitPlusOne() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertEquals(11, param.getQueryLimit());
  }

  @Test
  void constructor_withCursor_setsCursor() {
    DateTime cursor = new DateTime(2024, 1, 1, 0, 0);
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(cursor, 10, Direction.NEXT);

    assertEquals(cursor, param.getCursor());
  }

  @Test
  void constructor_withNullCursor_setsNullCursor() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 10, Direction.NEXT);

    assertNull(param.getCursor());
  }

  @Test
  void defaultConstructor_setsDefaultLimit() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>();

    assertEquals(20, param.getLimit());
  }

  @Test
  void constructor_withLimitAtBoundary_setsCorrectly() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 1000, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void constructor_withLimitJustOverBoundary_capsAtMax() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 1001, Direction.NEXT);

    assertEquals(1000, param.getLimit());
  }

  @Test
  void constructor_withLimitOne_setsOne() {
    CursorPageParameter<DateTime> param = new CursorPageParameter<>(null, 1, Direction.NEXT);

    assertEquals(1, param.getLimit());
  }
}
