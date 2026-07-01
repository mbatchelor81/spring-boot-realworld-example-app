package io.spring.infrastructure.service;

import io.jsonwebtoken.Jwts;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.util.Date;
import java.util.Optional;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultJwtServiceTest {

  private static final String TEST_SECRET =
      "1231231231231231231231231231231231231231231231231231231231231231231231231231231231231231";
  private JwtService jwtService;

  @BeforeEach
  public void setUp() {
    jwtService = new DefaultJwtService(TEST_SECRET, 3600);
  }

  @Test
  public void should_generate_and_parse_token() {
    User user = new User("email@email.com", "username", "123", "", "");
    String token = jwtService.toToken(user);
    Assertions.assertNotNull(token);
    Optional<String> optional = jwtService.getSubFromToken(token);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), user.getId());
  }

  @Test
  public void should_get_null_with_wrong_jwt() {
    Optional<String> optional = jwtService.getSubFromToken("123");
    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_get_null_with_expired_jwt() {
    SecretKeySpec key = new SecretKeySpec(TEST_SECRET.getBytes(), "HmacSHA512");
    String token =
        Jwts.builder()
            .subject("testuser")
            .expiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    Assertions.assertFalse(jwtService.getSubFromToken(token).isPresent());
  }
}
