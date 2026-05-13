package com.ftgo.security;

import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnProperty(
    prefix = "ftgo.security",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoSecurityAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(PasswordEncoder.class)
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @ConditionalOnMissingBean(SecurityFilterChain.class)
  public SecurityFilterChain ftgoSecurityFilterChain(
      HttpSecurity http, FtgoSecurityProperties properties) throws Exception {

    String[] publicPaths = properties.getPublicPaths().toArray(new String[0]);

    http.csrf()
        .disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .cors()
        .and()
        .authorizeRequests()
        .antMatchers(publicPaths)
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic();

    return http.build();
  }

  @Bean
  @ConditionalOnMissingBean(CorsConfigurationSource.class)
  public CorsConfigurationSource corsConfigurationSource(FtgoSecurityProperties properties) {
    FtgoSecurityProperties.Cors corsProps = properties.getCors();

    CorsConfiguration configuration = new CorsConfiguration();
    List<String> origins = corsProps.getAllowedOrigins();
    boolean hasWildcard = origins.stream().anyMatch(o -> o.contains("*"));
    if (hasWildcard) {
      configuration.setAllowedOriginPatterns(origins);
    } else {
      configuration.setAllowedOrigins(origins);
    }
    configuration.setAllowedMethods(corsProps.getAllowedMethods());
    configuration.setAllowedHeaders(corsProps.getAllowedHeaders());
    configuration.setAllowCredentials(corsProps.isAllowCredentials());
    configuration.setMaxAge(corsProps.getMaxAge());
    configuration.setExposedHeaders(corsProps.getExposedHeaders());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
