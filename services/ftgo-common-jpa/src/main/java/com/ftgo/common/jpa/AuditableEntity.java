package com.ftgo.common.jpa;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * Extends {@link BaseEntity} with automatic {@code createdAt} / {@code updatedAt} timestamps. The
 * fields are populated via JPA lifecycle callbacks and are never {@code null} once the entity is
 * persisted.
 */
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
