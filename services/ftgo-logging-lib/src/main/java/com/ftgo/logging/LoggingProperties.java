package com.ftgo.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ftgo.logging")
public class LoggingProperties {

  private boolean jsonEnabled = true;
  private boolean asyncEnabled = true;
  private int asyncQueueSize = 512;
  private int asyncDiscardingThreshold = 0;

  public boolean isJsonEnabled() {
    return jsonEnabled;
  }

  public void setJsonEnabled(boolean jsonEnabled) {
    this.jsonEnabled = jsonEnabled;
  }

  public boolean isAsyncEnabled() {
    return asyncEnabled;
  }

  public void setAsyncEnabled(boolean asyncEnabled) {
    this.asyncEnabled = asyncEnabled;
  }

  public int getAsyncQueueSize() {
    return asyncQueueSize;
  }

  public void setAsyncQueueSize(int asyncQueueSize) {
    this.asyncQueueSize = asyncQueueSize;
  }

  public int getAsyncDiscardingThreshold() {
    return asyncDiscardingThreshold;
  }

  public void setAsyncDiscardingThreshold(int asyncDiscardingThreshold) {
    this.asyncDiscardingThreshold = asyncDiscardingThreshold;
  }
}
