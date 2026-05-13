package com.ftgo.security.jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class FtgoUserContext {

  private static volatile String rolePrefix = "ROLE_";
  private static volatile String userIdClaim = "sub";

  private FtgoUserContext() {}

  static void setRolePrefix(String prefix) {
    rolePrefix = prefix;
  }

  static void setUserIdClaim(String claim) {
    userIdClaim = claim;
  }

  public static Optional<String> getUserId() {
    return getJwt().map(jwt -> jwt.getClaimAsString(userIdClaim));
  }

  public static Optional<String> getUsername() {
    return getAuthentication()
        .filter(auth -> auth instanceof JwtAuthenticationToken)
        .map(Authentication::getName);
  }

  public static List<String> getRoles() {
    return getAuthentication()
        .map(Authentication::getAuthorities)
        .map(FtgoUserContext::authorityNames)
        .map(
            names ->
                names.stream().filter(n -> n.startsWith(rolePrefix)).collect(Collectors.toList()))
        .orElse(Collections.emptyList());
  }

  public static Optional<String> getClaim(String claimName) {
    return getJwt().map(jwt -> jwt.getClaimAsString(claimName));
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> getClaim(String claimName, Class<T> type) {
    return getJwt().map(jwt -> jwt.getClaim(claimName)).filter(type::isInstance).map(type::cast);
  }

  public static Map<String, Object> getAllClaims() {
    return getJwt().map(Jwt::getClaims).orElse(Collections.emptyMap());
  }

  public static boolean isAuthenticated() {
    return getAuthentication().map(Authentication::isAuthenticated).orElse(false);
  }

  public static boolean hasRole(String role) {
    return getRoles().stream().anyMatch(r -> r.equals(role) || r.equals(rolePrefix + role));
  }

  public static Optional<Jwt> getJwt() {
    return getAuthentication()
        .filter(auth -> auth instanceof JwtAuthenticationToken)
        .map(auth -> ((JwtAuthenticationToken) auth).getToken());
  }

  private static Optional<Authentication> getAuthentication() {
    return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
  }

  private static List<String> authorityNames(Collection<? extends GrantedAuthority> authorities) {
    return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
  }
}
