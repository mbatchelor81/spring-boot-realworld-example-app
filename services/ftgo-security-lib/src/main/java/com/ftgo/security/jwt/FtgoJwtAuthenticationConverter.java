package com.ftgo.security.jwt;

import com.ftgo.security.FtgoSecurityProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class FtgoJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final FtgoSecurityProperties.Jwt jwtProperties;

  public FtgoJwtAuthenticationConverter(FtgoSecurityProperties.Jwt jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
    String principalName = extractPrincipalName(jwt);
    return new JwtAuthenticationToken(jwt, authorities, principalName);
  }

  private String extractPrincipalName(Jwt jwt) {
    String usernameClaim = jwtProperties.getUsernameClaim();
    String username = jwt.getClaimAsString(usernameClaim);
    if (username != null) {
      return username;
    }
    return jwt.getSubject();
  }

  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> roles = new ArrayList<>();

    String rolesClaim = jwtProperties.getRolesClaim();
    if (rolesClaim.contains(".")) {
      roles.addAll(extractNestedRoles(jwt, rolesClaim));
    } else {
      Object claimValue = jwt.getClaim(rolesClaim);
      if (claimValue instanceof List) {
        roles.addAll((List<String>) claimValue);
      }
    }

    Object scopeClaim = jwt.getClaim("scope");
    if (scopeClaim instanceof String) {
      String[] scopes = ((String) scopeClaim).split("\\s+");
      for (String scope : scopes) {
        roles.add("SCOPE_" + scope);
      }
    }

    String prefix = jwtProperties.getRolePrefix();
    return roles.stream()
        .map(role -> role.startsWith(prefix) ? role : prefix + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private List<String> extractNestedRoles(Jwt jwt, String claimPath) {
    String[] parts = claimPath.split("\\.");
    Object current = jwt.getClaims();

    for (int i = 0; i < parts.length; i++) {
      if (current instanceof Map) {
        current = ((Map<String, Object>) current).get(parts[i]);
      } else {
        return Collections.emptyList();
      }
    }

    if (current instanceof List) {
      return (List<String>) current;
    }
    return Collections.emptyList();
  }
}
