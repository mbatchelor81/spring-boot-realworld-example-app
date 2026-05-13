/**
 * Shared JPA base entities, converters, and persistence utilities used across all FTGO
 * microservices.
 *
 * <ul>
 *   <li>{@link com.ftgo.common.jpa.BaseEntity} — mapped-superclass with auto-generated ID
 *   <li>{@link com.ftgo.common.jpa.AuditableEntity} — adds createdAt/updatedAt audit fields
 *   <li>{@link com.ftgo.common.jpa.MoneyConverter} — JPA converter for {@link
 *       com.ftgo.common.Money}
 *   <li>{@link com.ftgo.common.jpa.JpaConfiguration} — Spring configuration that imports common
 *       config
 * </ul>
 */
package com.ftgo.common.jpa;
