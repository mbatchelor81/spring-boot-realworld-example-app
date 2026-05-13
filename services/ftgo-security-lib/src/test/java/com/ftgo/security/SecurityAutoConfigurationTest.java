package com.ftgo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class SecurityAutoConfigurationTest {

  @Autowired private ApplicationContext context;

  @Autowired private MockMvc mockMvc;

  @Test
  void securityFilterChainBeanIsRegistered() {
    assertThat(context.getBean(SecurityFilterChain.class)).isNotNull();
  }

  @Test
  void passwordEncoderBeanIsBCrypt() {
    PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
    assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
  }

  @Test
  void corsConfigurationSourceBeanIsRegistered() {
    assertThat(context.getBean(CorsConfigurationSource.class)).isNotNull();
  }

  @Test
  void securityPropertiesBeanIsRegistered() {
    FtgoSecurityProperties props = context.getBean(FtgoSecurityProperties.class);
    assertThat(props).isNotNull();
    assertThat(props.getPublicPaths()).contains("/actuator/health", "/actuator/info");
  }

  @Test
  void healthEndpointIsAccessibleWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
  }

  @Test
  void infoEndpointIsAccessibleWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
  }

  @Test
  void securedEndpointReturns401WithoutAuthentication() throws Exception {
    mockMvc.perform(get("/api/test")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void securedEndpointReturns200WithAuthentication() throws Exception {
    mockMvc.perform(get("/api/test")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void postRequestSucceedsWithoutCsrfToken() throws Exception {
    mockMvc.perform(post("/api/test")).andExpect(status().isOk());
  }

  @Test
  void otherActuatorEndpointsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
  }
}
