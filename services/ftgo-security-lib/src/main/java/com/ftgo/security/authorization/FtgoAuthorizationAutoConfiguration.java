package com.ftgo.security.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@Configuration(proxyBeanMethods = false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FtgoAuthorizationAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(RoleHierarchy.class)
  public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy(
        "ROLE_ADMIN > ROLE_RESTAURANT_OWNER\n"
            + "ROLE_ADMIN > ROLE_COURIER\n"
            + "ROLE_RESTAURANT_OWNER > ROLE_CUSTOMER\n"
            + "ROLE_COURIER > ROLE_CUSTOMER");
    return hierarchy;
  }

  @Bean
  @ConditionalOnMissingBean(FtgoPermissionEvaluator.class)
  public FtgoPermissionEvaluator ftgoPermissionEvaluator() {
    return new FtgoPermissionEvaluator();
  }

  @Bean
  @ConditionalOnMissingBean(ResourceOwnershipEvaluator.class)
  public ResourceOwnershipEvaluator resourceOwnershipEvaluator() {
    return new ResourceOwnershipEvaluator();
  }

  @Bean
  @ConditionalOnMissingBean(MethodSecurityExpressionHandler.class)
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy, FtgoPermissionEvaluator permissionEvaluator) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    handler.setPermissionEvaluator(permissionEvaluator);
    return handler;
  }
}
