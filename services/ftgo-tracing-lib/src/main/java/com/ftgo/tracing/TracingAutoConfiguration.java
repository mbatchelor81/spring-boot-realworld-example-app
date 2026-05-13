package com.ftgo.tracing;

import brave.Tracing;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.B3Propagation;
import brave.propagation.CurrentTraceContext;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
  public ZipkinSpanHandler zipkinSpanHandler(AsyncReporter<zipkin2.Span> reporter) {
    return (ZipkinSpanHandler) ZipkinSpanHandler.create(reporter);
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
      ZipkinSpanHandler zipkinSpanHandler,
      TracingProperties properties) {
    return Tracing.newBuilder()
        .localServiceName(serviceName)
        .currentTraceContext(currentTraceContext)
        .sampler(Sampler.create(properties.getSamplingRate()))
        .propagationFactory(B3Propagation.FACTORY)
        .addSpanHandler(zipkinSpanHandler)
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
}
