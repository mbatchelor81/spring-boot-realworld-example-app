package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class DateTimeCursorTest {

  @Test
  void toString_returnsMillisAsString() {
    DateTime dt = new DateTime(2024, 1, 1, 0, 0, 0);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals(String.valueOf(dt.getMillis()), cursor.toString());
  }

  @Test
  void parse_withValidMillisString_returnsDateTime() {
    DateTime original = new DateTime(2024, 6, 15, 12, 30, 0);
    String millis = String.valueOf(original.getMillis());

    DateTime parsed = DateTimeCursor.parse(millis);

    assertEquals(original.getMillis(), parsed.getMillis());
  }

  @Test
  void getData_returnsDateTime() {
    DateTime dt = new DateTime(2024, 1, 1, 0, 0, 0);
    DateTimeCursor cursor = new DateTimeCursor(dt);

    assertEquals(dt, cursor.getData());
  }

  @Test
  void roundTrip_preservesDateTime() {
    DateTime original = new DateTime(2024, 3, 20, 8, 45, 30);
    DateTimeCursor cursor = new DateTimeCursor(original);

    DateTime parsed = DateTimeCursor.parse(cursor.toString());

    assertEquals(original.getMillis(), parsed.getMillis());
  }
}
