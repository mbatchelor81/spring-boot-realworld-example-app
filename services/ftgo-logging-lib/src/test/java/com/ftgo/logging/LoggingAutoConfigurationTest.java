package com.ftgo.logging;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

class LoggingAutoConfigurationTest {

  private final WebApplicationContextRunner contextRunner =
      new WebApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

  @Test
  void registersCorrelationIdFilter() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(CorrelationIdFilter.class);
          assertThat(context).hasBean("correlationIdFilterRegistration");
        });
  }

  @Test
  void usesApplicationNameAsServiceName() {
    contextRunner
        .withPropertyValues("spring.application.name=my-service")
        .run(
            context -> {
              assertThat(context).hasSingleBean(CorrelationIdFilter.class);
            });
  }

  @Test
  void filterRegistrationMatchesAllUrls() {
    contextRunner.run(
        context -> {
          @SuppressWarnings("unchecked")
          FilterRegistrationBean<CorrelationIdFilter> registration =
              context.getBean("correlationIdFilterRegistration", FilterRegistrationBean.class);
          assertThat(registration.getUrlPatterns()).containsExactly("/*");
        });
  }

  @Test
  void backsOffWhenCustomFilterProvided() {
    contextRunner
        .withBean(CorrelationIdFilter.class, () -> new CorrelationIdFilter("custom-service"))
        .run(
            context -> {
              assertThat(context).hasSingleBean(CorrelationIdFilter.class);
            });
  }

  @Test
  void bindsLoggingProperties() {
    contextRunner
        .withPropertyValues(
            "ftgo.logging.json-enabled=false",
            "ftgo.logging.async-queue-size=1024")
        .run(
            context -> {
              assertThat(context).hasSingleBean(LoggingProperties.class);
              LoggingProperties props = context.getBean(LoggingProperties.class);
              assertThat(props.isJsonEnabled()).isFalse();
              assertThat(props.getAsyncQueueSize()).isEqualTo(1024);
            });
  }
}
