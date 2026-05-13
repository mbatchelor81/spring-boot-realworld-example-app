package com.ftgo.testlib.mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables all security for controller tests where
 * authentication/authorization is not the subject under test.
 *
 * <p>Usage:
 *
 * <pre>
 * &#064;WebMvcTest(OrderController.class)
 * &#064;Import(MockSecurityConfig.class)
 * class OrderControllerTest { ... }
 * </pre>
 */
@TestConfiguration
public class MockSecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeRequests().anyRequest().permitAll();
    return http.build();
  }
}
