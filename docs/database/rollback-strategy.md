# Rollback and Data Consistency Strategy

**Jira:** EM-29  
**Status:** Accepted  
**Date:** 2026-05-13

---

## 1. Overview

This document defines how to roll back database changes during and after the
per-service database migration. It covers Flyway rollback procedures,
data consistency verification, and emergency fallback to the shared database.

## 2. Flyway Rollback Procedures

### 2.1 Undo Migrations (Flyway Teams)

Flyway Teams edition supports `U<n>__<description>.sql` undo scripts that
reverse the corresponding `V<n>` migration. For each V1 migration, an undo
script should be prepared for production use:

| Service            | Migration                           | Undo Script                          |
|--------------------|-------------------------------------|--------------------------------------|
| consumer-service   | `V1__create_consumer_schema.sql`    | `U1__drop_consumer_schema.sql`       |
| restaurant-service | `V1__create_restaurant_schema.sql`  | `U1__drop_restaurant_schema.sql`     |
| order-service      | `V1__create_order_schema.sql`       | `U1__drop_order_schema.sql`          |
| courier-service    | `V1__create_courier_schema.sql`     | `U1__drop_courier_schema.sql`        |

### 2.2 Manual Rollback (Flyway Community)

For Flyway Community edition, rollback is manual:

```bash
# 1. Drop all tables created by the migration
mysql -u root -p ftgo_consumer_db -e "DROP TABLE IF EXISTS consumers;"

# 2. Remove the Flyway history entry
mysql -u root -p ftgo_consumer_db -e \
  "DELETE FROM flyway_schema_history WHERE version = '1';"
```

### 2.3 Rollback Rules

1. **Never roll back in production without a data backup.**
2. **Always test the rollback script in staging first.**
3. **Coordinate rollback across services** — if order-service rolls back,
   courier-service may also need to roll back (they share order ID
   references).
4. **Rollback window** — Rollback is safe until data has been written to
   the new per-service tables. After data exists, a data migration script
   is required to move it back to the shared database.

## 3. Data Consistency Verification

### 3.1 Pre-Migration Checks

Before splitting the database, verify that the monolith data is consistent:

```sql
-- Verify all order consumer_ids exist in consumers table
SELECT o.id, o.consumer_id
FROM orders o
LEFT JOIN consumers c ON o.consumer_id = c.id
WHERE c.id IS NULL;

-- Verify all order restaurant references exist
SELECT o.id, o.restaurant_id
FROM orders o
LEFT JOIN restaurants r ON o.restaurant_id = r.id
WHERE r.id IS NULL;

-- Verify all courier_actions reference valid orders and couriers
SELECT ca.courier_id, ca.order_id
FROM courier_actions ca
LEFT JOIN courier c ON ca.courier_id = c.id
LEFT JOIN orders o ON ca.order_id = o.id
WHERE c.id IS NULL OR o.id IS NULL;
```

### 3.2 Post-Migration Checks

After splitting, run per-service integrity checks:

```sql
-- order-service: verify all referenced IDs were migrated
SELECT COUNT(*) AS orphaned_consumer_refs
FROM orders o
WHERE o.consumer_id NOT IN (
    SELECT consumer_id FROM consumer_cache
);

-- courier-service: verify all referenced order IDs exist
SELECT COUNT(*) AS orphaned_order_refs
FROM courier_actions ca
WHERE ca.order_id NOT IN (
    -- Query order-service API to validate
    SELECT id FROM order_cache
);
```

### 3.3 Ongoing Consistency Monitoring

Each service exposes a health check endpoint that reports data consistency
metrics:

| Metric                              | Alert Threshold |
|-------------------------------------|-----------------|
| `orphaned_cross_service_refs`       | > 0             |
| `event_processing_lag_seconds`      | > 300           |
| `failed_event_processing_count`     | > 10            |
| `cache_staleness_seconds`           | > 600           |

## 4. Emergency Fallback: Shared Database

If the per-service database migration fails in production, services can fall
back to the shared monolith database:

### 4.1 Fallback Steps

1. **Stop all microservices.**
2. **Reconfigure `application.yml`** in each service to point to the shared
   `ftgo` database.
3. **Disable per-service Flyway** — set `spring.flyway.enabled: false` to
   prevent per-service migrations from running against the shared DB.
4. **Restart services** — they will read/write from the shared database.
5. **Investigate and fix** the root cause before attempting the split again.

### 4.2 Data Reconciliation

If data was written to per-service databases before fallback:

1. Export data from each per-service database.
2. Merge into the shared database, resolving ID conflicts.
3. Verify referential integrity with the pre-migration check queries above.

## 5. Backup Strategy

| Phase              | Backup Frequency | Backup Scope             | Retention |
|--------------------|------------------|--------------------------|-----------|
| Pre-migration      | Daily            | Full `ftgo` database     | 30 days   |
| During migration   | Before each step | Full `ftgo` + per-service DBs | 90 days |
| Post-migration     | Daily            | All per-service databases | 30 days   |

### Backup Commands

```bash
# Full monolith backup (pre-migration)
mysqldump -u root -p ftgo > ftgo_backup_$(date +%Y%m%d).sql

# Per-service backup (post-migration)
for db in ftgo_consumer_db ftgo_restaurant_db ftgo_order_db ftgo_courier_db; do
    mysqldump -u root -p $db > ${db}_backup_$(date +%Y%m%d).sql
done
```
