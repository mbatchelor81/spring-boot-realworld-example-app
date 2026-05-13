package com.ftgo.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.security")
public class FtgoSecurityProperties {

  private boolean enabled = true;

  private List<String> publicPaths = Arrays.asList("/actuator/health", "/actuator/info");

  private Cors cors = new Cors();

  private Jwt jwt = new Jwt();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<String> getPublicPaths() {
    return publicPaths;
  }

  public void setPublicPaths(List<String> publicPaths) {
    this.publicPaths = publicPaths;
  }

  public Cors getCors() {
    return cors;
  }

  public void setCors(Cors cors) {
    this.cors = cors;
  }

  public Jwt getJwt() {
    return jwt;
  }

  public void setJwt(Jwt jwt) {
    this.jwt = jwt;
  }

  public static class Jwt {

    private boolean enabled = false;

    private String issuerUri;

    private String jwkSetUri;

    private List<String> audiences = Collections.emptyList();

    private String usernameClaim = "preferred_username";

    private String userIdClaim = "sub";

    private String rolesClaim = "realm_access.roles";

    private String rolePrefix = "ROLE_";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getIssuerUri() {
      return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
      this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
      return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
      this.jwkSetUri = jwkSetUri;
    }

    public List<String> getAudiences() {
      return audiences;
    }

    public void setAudiences(List<String> audiences) {
      this.audiences = audiences;
    }

    public String getUsernameClaim() {
      return usernameClaim;
    }

    public void setUsernameClaim(String usernameClaim) {
      this.usernameClaim = usernameClaim;
    }

    public String getUserIdClaim() {
      return userIdClaim;
    }

    public void setUserIdClaim(String userIdClaim) {
      this.userIdClaim = userIdClaim;
    }

    public String getRolesClaim() {
      return rolesClaim;
    }

    public void setRolesClaim(String rolesClaim) {
      this.rolesClaim = rolesClaim;
    }

    public String getRolePrefix() {
      return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix) {
      this.rolePrefix = rolePrefix;
    }
  }

  public static class Cors {

    private List<String> allowedOrigins = Arrays.asList("*");

    private List<String> allowedMethods =
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    private List<String> allowedHeaders = Arrays.asList("*");

    private boolean allowCredentials = false;

    private long maxAge = 3600;

    private List<String> exposedHeaders = Arrays.asList("Authorization", "Content-Type");

    public List<String> getAllowedOrigins() {
      return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
      this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
      return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
      this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
      return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
      this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
      return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
      return maxAge;
    }

    public void setMaxAge(long maxAge) {
      this.maxAge = maxAge;
    }

    public List<String> getExposedHeaders() {
      return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
      this.exposedHeaders = exposedHeaders;
    }
  }
}
