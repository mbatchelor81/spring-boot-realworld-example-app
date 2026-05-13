package com.ftgo.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MaskingConverterTest {

  @Test
  void masksCreditCardNumbers() {
    String input = "Payment with card 4111111111111111 processed";
    String result = MaskingConverter.maskSensitiveData(input);
    assertFalse(result.contains("4111111111111111"));
    assertTrue(result.contains("4111"));
    assertTrue(result.contains("1111"));
  }

  @Test
  void masksPasswordFields() {
    String input = "User login with password=secret123 completed";
    String result = MaskingConverter.maskSensitiveData(input);
    assertFalse(result.contains("secret123"));
    assertTrue(result.contains("password=********"));
  }

  @Test
  void masksBearerTokens() {
    String input = "Authorization header Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig";
    String result = MaskingConverter.maskSensitiveData(input);
    assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9"));
    assertTrue(result.contains("Bearer [REDACTED]"));
  }

  @Test
  void masksAuthorizationHeaders() {
    String input = "Header Authorization: BasicABC123DEF";
    String result = MaskingConverter.maskSensitiveData(input);
    assertFalse(result.contains("BasicABC123DEF"));
    assertTrue(result.contains("Authorization=[REDACTED]"));
  }

  @Test
  void masksAuthorizationBearerCombined() {
    String input = "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig";
    String result = MaskingConverter.maskSensitiveData(input);
    assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9"));
    assertTrue(result.contains("Authorization=[REDACTED]"));
    assertFalse(result.contains("Bearer"), "Bearer keyword should not remain after masking");
  }

  @Test
  void preservesNonSensitiveContent() {
    String input = "Order 12345 created for user john";
    String result = MaskingConverter.maskSensitiveData(input);
    assertEquals(input, result);
  }

  @Test
  void handlesNullMessage() {
    MaskingConverter converter = new MaskingConverter();
    assertNull(converter.convert(new TestLoggingEvent(null)));
  }

  private static class TestLoggingEvent extends ch.qos.logback.classic.spi.LoggingEvent {
    private final String message;

    TestLoggingEvent(String message) {
      this.message = message;
    }

    @Override
    public String getFormattedMessage() {
      return message;
    }
  }
}
