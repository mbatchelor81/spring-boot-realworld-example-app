package com.ftgo.testlib.containers;

import org.testcontainers.containers.MySQLContainer;

/**
 * Factory for Testcontainers instances used across FTGO integration tests.
 *
 * <p>Centralises container configuration so that all services use consistent database
 * versions, schemas, and credentials.
 *
 * <p>Usage:
 *
 * <pre>
 * &#064;Testcontainers
 * class MyIntegrationTest {
 *
 *     &#064;Container
 *     static MySQLContainer&lt;?&gt; mysql = FtgoContainers.mysql();
 *
 *     &#064;DynamicPropertySource
 *     static void dbProperties(DynamicPropertyRegistry registry) {
 *         FtgoContainers.configureMysql(registry, mysql);
 *     }
 * }
 * </pre>
 */
public final class FtgoContainers {

  private static final String MYSQL_IMAGE = "mysql:8.0";
  private static final String DATABASE_NAME = "ftgo_test";
  private static final String USERNAME = "ftgo_test";
  private static final String PASSWORD = "ftgo_test";

  private FtgoContainers() {}

  /**
   * Creates a pre-configured MySQL container for FTGO integration tests.
   *
   * @return a configured {@link MySQLContainer} instance
   */
  public static MySQLContainer<?> mysql() {
    return new MySQLContainer<>(MYSQL_IMAGE)
        .withDatabaseName(DATABASE_NAME)
        .withUsername(USERNAME)
        .withPassword(PASSWORD)
        .withReuse(true);
  }

  /**
   * Configures Spring Boot datasource properties to point at the given MySQL container.
   *
   * @param registry the dynamic property registry from {@code @DynamicPropertySource}
   * @param mysql the running MySQL container
   */
  public static void configureMysql(
      org.springframework.test.context.DynamicPropertyRegistry registry,
      MySQLContainer<?> mysql) {
    registry.add("spring.datasource.url", mysql::getJdbcUrl);
    registry.add("spring.datasource.username", mysql::getUsername);
    registry.add("spring.datasource.password", mysql::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    registry.add("spring.flyway.enabled", () -> "true");
  }
}
