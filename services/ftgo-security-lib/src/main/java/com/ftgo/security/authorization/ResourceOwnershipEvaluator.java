package com.ftgo.security.authorization;

import com.ftgo.security.jwt.FtgoUserContext;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public class ResourceOwnershipEvaluator {

  public boolean isOwner(Authentication authentication, String resourceOwnerId) {
    if (authentication == null || resourceOwnerId == null) {
      return false;
    }
    Optional<String> currentUserId = FtgoUserContext.getUserId();
    return currentUserId.isPresent() && currentUserId.get().equals(resourceOwnerId);
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
