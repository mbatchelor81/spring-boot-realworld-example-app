package com.ftgo.test.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ftgo.security.jwt.FtgoJwtAuthenticationConverter;
import com.ftgo.security.jwt.JwtTokenProvider;
import com.nimbusds.jwt.JWTClaimsSet;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = JwtTestApplication.class,
    properties = {
      "ftgo.security.jwt.enabled=true",
      "ftgo.security.jwt.issuer-uri=http://localhost:9080/realms/ftgo"
    })
@AutoConfigureMockMvc
class JwtAuthenticationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ApplicationContext context;

  @Autowired private JwtTokenProvider tokenProvider;

  private String validToken;
  private String adminToken;
  private String expiredToken;

  @BeforeEach
  void setUp() {
    validToken = tokenProvider.createToken("user-123", "testuser", Arrays.asList("ftgo-consumer"));

    adminToken =
        tokenProvider.createToken(
            "admin-001", "admin", Arrays.asList("ftgo-admin", "ftgo-consumer"));

    JwtTokenProvider expiredProvider =
        new JwtTokenProvider(tokenProvider.getRsaKey(), tokenProvider.getIssuer(), -60);
    expiredToken =
        expiredProvider.createToken(
            "user-expired", "expireduser", Collections.singletonList("ftgo-consumer"));
  }

  @Test
  void jwtDecoderBeanIsRegistered() {
    assertThat(context.getBean(JwtDecoder.class)).isNotNull();
  }

  @Test
  void jwtAuthenticationConverterBeanIsRegistered() {
    assertThat(context.getBean(FtgoJwtAuthenticationConverter.class)).isNotNull();
  }

  @Test
  void protectedEndpointReturns401WithoutToken() throws Exception {
    mockMvc.perform(get("/api/test")).andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpointReturns200WithValidToken() throws Exception {
    mockMvc
        .perform(get("/api/test").header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void protectedEndpointReturns401WithExpiredToken() throws Exception {
    mockMvc
        .perform(get("/api/test").header("Authorization", "Bearer " + expiredToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void protectedEndpointReturns401WithInvalidToken() throws Exception {
    mockMvc
        .perform(get("/api/test").header("Authorization", "Bearer invalid.jwt.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void publicEndpointAccessibleWithoutToken() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void userContextEndpointReturnsClaimsFromToken() throws Exception {
    String response =
        mockMvc
            .perform(get("/api/me").header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("user-123");
    assertThat(response).contains("testuser");
  }

  @Test
  void adminTokenContainsMultipleRoles() throws Exception {
    String response =
        mockMvc
            .perform(get("/api/me").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("admin-001");
    assertThat(response).contains("ROLE_ftgo-admin");
    assertThat(response).contains("ROLE_ftgo-consumer");
  }

  @Test
  void tokenProviderValidatesOwnTokens() {
    assertThat(tokenProvider.validateToken(validToken)).isTrue();
    assertThat(tokenProvider.validateToken(expiredToken)).isFalse();
    assertThat(tokenProvider.validateToken("garbage")).isFalse();
  }

  @Test
  void tokenProviderParsesClaimsCorrectly() {
    JWTClaimsSet claims = tokenProvider.parseToken(validToken);
    assertThat(claims.getSubject()).isEqualTo("user-123");
    assertThat(claims.getIssuer()).isEqualTo("http://localhost:9080/realms/ftgo");
    assertThat(claims.getClaim("preferred_username")).isEqualTo("testuser");
  }
}
