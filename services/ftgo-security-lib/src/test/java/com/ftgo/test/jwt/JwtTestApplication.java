package com.ftgo.test.jwt;

import com.ftgo.security.jwt.FtgoUserContext;
import com.ftgo.security.jwt.JwtTokenProvider;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableAutoConfiguration
public class JwtTestApplication {

  private static final String TEST_ISSUER = "http://localhost:9080/realms/ftgo";

  @Bean
  public JwtTokenProvider jwtTokenProvider() {
    return JwtTokenProvider.withGeneratedKey(TEST_ISSUER, 3600);
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtTokenProvider tokenProvider) {
    try {
      RSAPublicKey publicKey = tokenProvider.getRsaKey().toRSAPublicKey();
      NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
      decoder.setJwtValidator(new JwtTimestampValidator());
      return decoder;
    } catch (com.nimbusds.jose.JOSEException e) {
      throw new IllegalStateException("Failed to build JwtDecoder", e);
    }
  }

  @RestController
  static class JwtTestController {

    @GetMapping("/api/test")
    public String getTest() {
      return "OK";
    }

    @PostMapping("/api/test")
    public String postTest() {
      return "CREATED";
    }

    @GetMapping("/api/me")
    public Map<String, Object> getMe() {
      return Map.of(
          "userId", FtgoUserContext.getUserId().orElse("anonymous"),
          "username", FtgoUserContext.getUsername().orElse("anonymous"),
          "roles", FtgoUserContext.getRoles(),
          "authenticated", FtgoUserContext.isAuthenticated());
    }
  }
}
