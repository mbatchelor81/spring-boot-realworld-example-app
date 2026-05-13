package com.ftgo.openapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared OpenAPI 3.0 auto-configuration for all FTGO microservices.
 *
 * <p>Each service should set the following properties in its {@code application.yml}:
 *
 * <pre>
 * ftgo.openapi.title: Consumer Service API
 * ftgo.openapi.description: Manages consumer registration and profiles
 * ftgo.openapi.version: 1.0.0
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(FtgoOpenApiProperties.class)
public class FtgoOpenApiAutoConfiguration {

  private static final String SECURITY_SCHEME_NAME = "bearerAuth";

  private final FtgoOpenApiProperties properties;

  public FtgoOpenApiAutoConfiguration(FtgoOpenApiProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public OpenAPI ftgoOpenApi() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(
            List.of(
                new Server()
                    .url(properties.getServerUrl())
                    .description("Local development server")))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        .components(
            new Components()
                .addSecuritySchemes(
                    SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT authentication token")));
  }

  private Info apiInfo() {
    return new Info()
        .title(properties.getTitle())
        .description(properties.getDescription())
        .version(properties.getVersion())
        .contact(
            new Contact()
                .name("FTGO Engineering")
                .email("engineering@ftgo.com")
                .url("https://github.com/mbatchelor81/spring-boot-realworld-example-app"))
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
  }
}
