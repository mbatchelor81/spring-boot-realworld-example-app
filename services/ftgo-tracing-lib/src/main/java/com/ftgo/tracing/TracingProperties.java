package com.ftgo.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.tracing")
public class TracingProperties {

  private String zipkinEndpoint = "http://zipkin:9411/api/v2/spans";
  private float samplingRate = 1.0f;
  private String propagationType = "B3";

  public String getZipkinEndpoint() {
    return zipkinEndpoint;
  }

  public void setZipkinEndpoint(String zipkinEndpoint) {
    this.zipkinEndpoint = zipkinEndpoint;
  }

  public float getSamplingRate() {
    return samplingRate;
  }

  public void setSamplingRate(float samplingRate) {
    this.samplingRate = samplingRate;
  }

  public String getPropagationType() {
    return propagationType;
  }

  public void setPropagationType(String propagationType) {
    this.propagationType = propagationType;
  }
}
