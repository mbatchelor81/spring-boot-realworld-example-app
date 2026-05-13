package com.ftgo.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class ResourceOwnershipEvaluator {

  private final String userIdClaim;

  public ResourceOwnershipEvaluator() {
    this("sub");
  }

  public ResourceOwnershipEvaluator(String userIdClaim) {
    this.userIdClaim = userIdClaim;
  }

  public boolean isOwner(Authentication authentication, String resourceOwnerId) {
    if (authentication == null || resourceOwnerId == null) {
      return false;
    }
    if (!(authentication instanceof JwtAuthenticationToken)) {
      return false;
    }
    Jwt jwt = ((JwtAuthenticationToken) authentication).getToken();
    String currentUserId = jwt.getClaimAsString(userIdClaim);
    return currentUserId != null && currentUserId.equals(resourceOwnerId);
  }

  public boolean isOwnerOrAdmin(Authentication authentication, String resourceOwnerId) {
    if (authentication == null) {
      return false;
    }
    if (isAdmin(authentication)) {
      return true;
    }
    return isOwner(authentication, resourceOwnerId);
  }

  public boolean isAdmin(Authentication authentication) {
    if (authentication == null) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals(FtgoRole.ADMIN));
  }
}
