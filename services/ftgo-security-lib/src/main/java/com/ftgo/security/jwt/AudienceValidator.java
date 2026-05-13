package com.ftgo.security.jwt;

import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

  private static final OAuth2Error INVALID_AUDIENCE =
      new OAuth2Error("invalid_token", "The required audience is missing", null);

  private final List<String> expectedAudiences;

  public AudienceValidator(List<String> expectedAudiences) {
    this.expectedAudiences = expectedAudiences;
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    List<String> tokenAudiences = jwt.getAudience();
    if (tokenAudiences != null) {
      for (String expected : expectedAudiences) {
        if (tokenAudiences.contains(expected)) {
          return OAuth2TokenValidatorResult.success();
        }
      }
    }
    return OAuth2TokenValidatorResult.failure(INVALID_AUDIENCE);
  }
}
