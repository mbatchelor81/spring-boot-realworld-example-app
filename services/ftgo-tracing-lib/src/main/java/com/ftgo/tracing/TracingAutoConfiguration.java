package com.ftgo.tracing;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.handler.SpanHandler;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import io.micrometer.tracing.brave.bridge.W3CPropagation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
@EnableConfigurationProperties(TracingProperties.class)
public class TracingAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public URLConnectionSender zipkinSender(TracingProperties properties) {
    return URLConnectionSender.create(properties.getZipkinEndpoint());
  }

  @Bean
  @ConditionalOnMissingBean
  public AsyncReporter<zipkin2.Span> zipkinReporter(URLConnectionSender sender) {
    return AsyncReporter.create(sender);
  }

  @Bean
  @ConditionalOnMissingBean
  public SpanHandler zipkinSpanHandler(AsyncReporter<zipkin2.Span> reporter) {
    return ZipkinSpanHandler.create(reporter);
  }

  @Bean
  @ConditionalOnMissingBean
  public CurrentTraceContext currentTraceContext() {
    return ThreadLocalCurrentTraceContext.newBuilder()
        .addScopeDecorator(MDCScopeDecorator.get())
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public Tracing braveTracing(
      @Value("${spring.application.name:unknown}") String serviceName,
      CurrentTraceContext currentTraceContext,
      SpanHandler spanHandler,
      TracingProperties properties) {

    Propagation.Factory propagationFactory;
    if ("W3C".equalsIgnoreCase(properties.getPropagationType())) {
      propagationFactory = new W3CPropagation();
    } else {
      propagationFactory = B3Propagation.FACTORY;
    }

    return Tracing.newBuilder()
        .localServiceName(serviceName)
        .currentTraceContext(currentTraceContext)
        .sampler(Sampler.create(properties.getSamplingRate()))
        .propagationFactory(propagationFactory)
        .addSpanHandler(spanHandler)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public brave.Tracer braveTracer(Tracing tracing) {
    return tracing.tracer();
  }

  @Bean
  @ConditionalOnMissingBean(Tracer.class)
  public Tracer micrometerTracer(Tracing tracing, CurrentTraceContext currentTraceContext) {
    return new BraveTracer(
        tracing.tracer(),
        new BraveCurrentTraceContext(currentTraceContext),
        new BraveBaggageManager());
  }

  @Bean
  @ConditionalOnMissingBean
  public TracingHelper tracingHelper(Tracer tracer) {
    return new TracingHelper(tracer);
  }

  @Bean
  public FilterRegistrationBean<TracingServletFilter> tracingFilter(Tracing tracing) {
    FilterRegistrationBean<TracingServletFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new TracingServletFilter(tracing));
    registration.addUrlPatterns("/*");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
    return registration;
  }
}
