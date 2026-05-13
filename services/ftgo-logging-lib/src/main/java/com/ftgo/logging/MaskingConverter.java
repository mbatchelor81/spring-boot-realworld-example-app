package com.ftgo.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logback converter that masks sensitive data (credit cards, passwords, tokens) in log messages.
 * Register via {@code <conversionRule>} in logback configuration.
 */
public class MaskingConverter extends ClassicConverter {

  private static final Pattern CREDIT_CARD_PATTERN =
      Pattern.compile("\\b([0-9]{4})[- ]?[0-9]{4}[- ]?([0-9]{2})[0-9]{2}[- ]?([0-9]{4})\\b");

  private static final Pattern PASSWORD_PATTERN =
      Pattern.compile(
          "(?i)(password|passwd|pwd|secret|credential)\\s*[=:]\\s*\"?([^\"\\s,;}{]+)\"?");

  private static final Pattern BEARER_TOKEN_PATTERN =
      Pattern.compile("(?i)(Bearer)\\s+[A-Za-z0-9\\-._~+/]+=*");

  private static final Pattern AUTHORIZATION_PATTERN =
      Pattern.compile("(?i)(Authorization)\\s*[=:]\\s*\"?([^\"}{;,\\n]+?)\\s*(?:\"|,|;|\\}|$)");

  @Override
  public String convert(ILoggingEvent event) {
    String message = event.getFormattedMessage();
    if (message == null) {
      return null;
    }
    return maskSensitiveData(message);
  }

  static String maskSensitiveData(String message) {
    String result = message;
    result = maskCreditCards(result);
    result = maskPasswords(result);
    result = maskAuthorization(result);
    result = maskBearerTokens(result);
    return result;
  }

  private static String maskCreditCards(String message) {
    Matcher matcher = CREDIT_CARD_PATTERN.matcher(message);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = matcher.group(1) + "********" + matcher.group(3);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static String maskPasswords(String message) {
    Matcher matcher = PASSWORD_PATTERN.matcher(message);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = matcher.group(1) + "=********";
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static String maskBearerTokens(String message) {
    Matcher matcher = BEARER_TOKEN_PATTERN.matcher(message);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = matcher.group(1) + " [REDACTED]";
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private static String maskAuthorization(String message) {
    Matcher matcher = AUTHORIZATION_PATTERN.matcher(message);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String replacement = matcher.group(1) + "=[REDACTED]";
      matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
}
