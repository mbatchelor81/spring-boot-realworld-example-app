package com.ftgo.error;

public final class ErrorCode {

  private ErrorCode() {}

  public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
  public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
  public static final String STATE_CONFLICT = "STATE_CONFLICT";
  public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
  public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
  public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
  public static final String ACCESS_DENIED = "ACCESS_DENIED";
  public static final String METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
  public static final String UNSUPPORTED_MEDIA_TYPE = "UNSUPPORTED_MEDIA_TYPE";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
