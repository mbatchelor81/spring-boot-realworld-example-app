package com.ftgo.security.authorization;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class FtgoPermissionEvaluator implements PermissionEvaluator {

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (authentication == null || permission == null) {
      return false;
    }
    String permissionStr = permission.toString();
    return hasAuthority(authentication, permissionStr);
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (authentication == null || permission == null) {
      return false;
    }
    String permissionStr = permission.toString();
    return hasAuthority(authentication, permissionStr);
  }

  private boolean hasAuthority(Authentication authentication, String permission) {
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals(permission));
  }
}
