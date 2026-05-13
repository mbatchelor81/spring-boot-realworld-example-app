package com.ftgo.openapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configurable OpenAPI properties for FTGO services. */
@ConfigurationProperties(prefix = "ftgo.openapi")
public class FtgoOpenApiProperties {

  private String title = "FTGO Service API";
  private String description = "FTGO microservice API";
  private String version = "1.0.0";
  private String serverUrl = "http://localhost:8080";

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }
}
