package com.ftgo.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.cloud.gateway.routes[0].id=test-route",
      "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
      "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/test/**",
      "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9080/realms/ftgo/protocol/openid-connect/certs",
      "spring.redis.host=localhost",
      "spring.redis.port=6379"
    })
@ActiveProfiles("test")
class FtgoApiGatewayApplicationTest {

  @Test
  void contextLoads() {}
}
