package com.ftgo.security.jwt;

import com.ftgo.security.FtgoSecurityProperties;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ftgo.security.jwt", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(FtgoSecurityProperties.class)
public class FtgoJwtAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(FtgoJwtAuthenticationConverter.class)
  public FtgoJwtAuthenticationConverter ftgoJwtAuthenticationConverter(
      FtgoSecurityProperties properties) {
    return new FtgoJwtAuthenticationConverter(properties.getJwt());
  }

  @Bean
  @ConditionalOnMissingBean(JwtDecoder.class)
  public JwtDecoder jwtDecoder(FtgoSecurityProperties properties) {
    FtgoSecurityProperties.Jwt jwtProps = properties.getJwt();
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwtProps.getJwkSetUri()).build();

    List<OAuth2TokenValidator<Jwt>> validators = new java.util.ArrayList<>();
    validators.add(new JwtTimestampValidator());

    String issuerUri = jwtProps.getIssuerUri();
    if (issuerUri != null && !issuerUri.isEmpty()) {
      validators.add(new JwtIssuerValidator(issuerUri));
    }

    List<String> audiences = jwtProps.getAudiences();
    if (audiences != null && !audiences.isEmpty()) {
      validators.add(new AudienceValidator(audiences));
    }

    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return decoder;
  }
}
