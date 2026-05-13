package com.ftgo.logging;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.AbstractFieldJsonProvider;
import net.logstash.logback.composite.JsonWritingUtils;

/**
 * LogstashEncoder provider that writes a masked version of the log message into the JSON output.
 * Replaces the default {@code message} field with sensitive data redacted.
 */
public class MaskingMessageProvider
    extends AbstractFieldJsonProvider<ch.qos.logback.classic.spi.ILoggingEvent> {

  public static final String FIELD_MESSAGE = "message";

  public MaskingMessageProvider() {
    setFieldName(FIELD_MESSAGE);
  }

  @Override
  public void writeTo(JsonGenerator generator, ch.qos.logback.classic.spi.ILoggingEvent event)
      throws IOException {
    String message = event.getFormattedMessage();
    if (message != null) {
      message = MaskingConverter.maskSensitiveData(message);
    }
    JsonWritingUtils.writeStringField(generator, getFieldName(), message);
  }
}
