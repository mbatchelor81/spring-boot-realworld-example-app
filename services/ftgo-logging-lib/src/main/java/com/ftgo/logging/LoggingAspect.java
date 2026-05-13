package com.ftgo.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect that logs method entry and exit for Spring {@code @Service} classes. Entry/exit are logged
 * at {@code DEBUG}; exceptions at {@code ERROR}.
 */
@Aspect
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Around("within(@org.springframework.stereotype.Service *)")
  public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();

    if (log.isDebugEnabled()) {
      String params = buildParamSummary(joinPoint.getArgs());
      log.debug("--> {}.{}({})", className, methodName, params);
    }

    long start = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      if (log.isDebugEnabled()) {
        long elapsed = System.currentTimeMillis() - start;
        log.debug("<-- {}.{} returned in {}ms", className, methodName, elapsed);
      }
      return result;
    } catch (Throwable ex) {
      long elapsed = System.currentTimeMillis() - start;
      log.error(
          "<-- {}.{} threw {} after {}ms",
          className,
          methodName,
          ex.getClass().getSimpleName(),
          elapsed,
          ex);
      throw ex;
    }
  }

  private static String buildParamSummary(Object[] args) {
    if (args == null || args.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < args.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(args[i] == null ? "null" : args[i].getClass().getSimpleName());
    }
    return sb.toString();
  }
}
