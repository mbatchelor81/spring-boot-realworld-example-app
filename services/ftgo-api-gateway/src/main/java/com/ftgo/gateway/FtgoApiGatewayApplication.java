package com.ftgo.gateway;

import com.ftgo.security.authorization.FtgoAuthorizationAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = FtgoAuthorizationAutoConfiguration.class)
public class FtgoApiGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(FtgoApiGatewayApplication.class, args);
  }
}
