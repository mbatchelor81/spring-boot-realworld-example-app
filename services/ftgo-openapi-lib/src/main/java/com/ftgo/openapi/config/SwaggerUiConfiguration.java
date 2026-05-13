package com.ftgo.openapi.config;

import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI configuration.
 *
 * <p>SpringDoc auto-configures Swagger UI at {@code /swagger-ui.html} (redirects to {@code
 * /swagger-ui/index.html}). The following {@code application.yml} properties customize the UI:
 *
 * <pre>
 * springdoc:
 *   swagger-ui:
 *     path: /swagger-ui.html
 *     operations-sorter: method
 *     tags-sorter: alpha
 *     try-it-out-enabled: true
 *     filter: true
 *     doc-expansion: list
 *   api-docs:
 *     path: /v3/api-docs
 * </pre>
 */
@Configuration
public class SwaggerUiConfiguration {}
