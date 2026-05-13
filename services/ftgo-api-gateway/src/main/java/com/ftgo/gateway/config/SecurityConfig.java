package com.ftgo.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Value("${ftgo.security.jwt.roles-claim:realm_access.roles}")
  private String rolesClaim;

  @Value("${ftgo.security.jwt.role-prefix:ROLE_}")
  private String rolePrefix;

  @Value("${ftgo.security.jwt.username-claim:preferred_username}")
  private String usernameClaim;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf()
        .disable()
        .authorizeExchange()
        .pathMatchers(
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/actuator/prometheus")
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .oauth2ResourceServer()
        .jwt()
        .jwtAuthenticationConverter(jwtAuthenticationConverter())
        .and()
        .and()
        .build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
    return jwt -> {
      Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
      String principalName = extractPrincipalName(jwt);
      return Mono.just(new JwtAuthenticationToken(jwt, authorities, principalName));
    };
  }

  private String extractPrincipalName(Jwt jwt) {
    String username = jwt.getClaimAsString(usernameClaim);
    return username != null ? username : jwt.getSubject();
  }

  @SuppressWarnings("unchecked")
  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<String> roles = new ArrayList<>();

    if (rolesClaim.contains(".")) {
      roles.addAll(extractNestedRoles(jwt, rolesClaim));
    } else {
      Object claimValue = jwt.getClaim(rolesClaim);
      if (claimValue instanceof List) {
        roles.addAll((List<String>) claimValue);
      }
    }

    List<GrantedAuthority> authorities =
        roles.stream()
            .map(role -> role.startsWith(rolePrefix) ? role : rolePrefix + role)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    Object scopeClaim = jwt.getClaim("scope");
    if (scopeClaim instanceof String) {
      String[] scopes = ((String) scopeClaim).split("\\s+");
      for (String scope : scopes) {
        authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
      }
    }

    return authorities;
  }

  @SuppressWarnings("unchecked")
  private List<String> extractNestedRoles(Jwt jwt, String claimPath) {
    String[] parts = claimPath.split("\\.");
    Object current = jwt.getClaims();

    for (String part : parts) {
      if (current instanceof Map) {
        current = ((Map<String, Object>) current).get(part);
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
