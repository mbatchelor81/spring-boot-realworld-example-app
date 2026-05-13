# Database Migration Runbook

**Jira:** EM-29  
**Status:** Draft  
**Date:** 2026-05-13  
**Audience:** Platform Engineering, SRE

---

## 1. Executive Summary

This runbook guides the transition from a single shared MySQL database
(`ftgo`) to four independent per-service databases. The migration is
performed in phases to minimize risk and allow rollback at each step.

## 2. Prerequisites

- [ ] MySQL 8.0+ available (or compatible: MariaDB 10.5+, Aurora MySQL 3.x)
- [ ] Database admin credentials for creating databases and users
- [ ] All monolith data backed up (see [Rollback Strategy](rollback-strategy.md))
- [ ] Per-service Flyway migrations tested in staging
- [ ] Application services deployable with per-service DB configuration
- [ ] Event infrastructure (message broker) available for Phase 3+

## 3. Migration Phases

### Phase 1: Create Per-Service Databases (No Downtime)

**Objective:** Create empty per-service databases alongside the existing
shared database.

```sql
-- Create databases
CREATE DATABASE IF NOT EXISTS ftgo_consumer_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ftgo_restaurant_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ftgo_order_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ftgo_courier_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create service users with least-privilege access
CREATE USER IF NOT EXISTS 'ftgo_consumer'@'%' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON ftgo_consumer_db.* TO 'ftgo_consumer'@'%';

CREATE USER IF NOT EXISTS 'ftgo_restaurant'@'%' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON ftgo_restaurant_db.* TO 'ftgo_restaurant'@'%';

CREATE USER IF NOT EXISTS 'ftgo_order'@'%' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON ftgo_order_db.* TO 'ftgo_order'@'%';

CREATE USER IF NOT EXISTS 'ftgo_courier'@'%' IDENTIFIED BY '<password>';
GRANT ALL PRIVILEGES ON ftgo_courier_db.* TO 'ftgo_courier'@'%';

FLUSH PRIVILEGES;
```

**Verification:**
```sql
SHOW DATABASES LIKE 'ftgo_%';
-- Expected: ftgo_consumer_db, ftgo_restaurant_db, ftgo_order_db, ftgo_courier_db
```

### Phase 2: Run Flyway Migrations (No Downtime)

**Objective:** Apply V1 schema migrations to each per-service database.

For each service, update `application.yml` to point to the new database
and start the service. Flyway will run automatically.

```yaml
# Example: ftgo-consumer-service application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ftgo_consumer_db
    username: ftgo_consumer
    password: ${CONSUMER_DB_PASSWORD}
  flyway:
    enabled: true
```

**Verification per service:**
```sql
-- Check Flyway history
SELECT * FROM ftgo_consumer_db.flyway_schema_history;
-- Expected: version=1, description='create consumer schema', success=1

-- Check table structure
SHOW TABLES FROM ftgo_consumer_db;
-- Expected: consumers, flyway_schema_history
```

Repeat for all four services.

### Phase 3: Migrate Data (Maintenance Window)

**Objective:** Copy existing data from the shared `ftgo` database to
per-service databases.

**Estimated duration:** Depends on data volume. For < 1M rows per table,
expect < 5 minutes.

**Maintenance window required:** Yes — writes must be paused during data
migration to prevent inconsistency.

#### Step 3.1: Stop All Write Traffic

```bash
# Option A: Set services to read-only mode
curl -X POST http://localhost:8081/actuator/maintenance?mode=readonly
curl -X POST http://localhost:8082/actuator/maintenance?mode=readonly
curl -X POST http://localhost:8083/actuator/maintenance?mode=readonly
curl -X POST http://localhost:8084/actuator/maintenance?mode=readonly

# Option B: Stop all services
systemctl stop ftgo-consumer-service
systemctl stop ftgo-restaurant-service
systemctl stop ftgo-order-service
systemctl stop ftgo-courier-service
```

#### Step 3.2: Backup Shared Database

```bash
mysqldump -u root -p ftgo > ftgo_pre_split_$(date +%Y%m%d_%H%M%S).sql
```

#### Step 3.3: Copy Data

```sql
-- Consumer data
INSERT INTO ftgo_consumer_db.consumers (id, first_name, last_name)
SELECT id, first_name, last_name FROM ftgo.consumers;

-- Restaurant data
INSERT INTO ftgo_restaurant_db.restaurants (id, name, street1, street2, city, state, zip)
SELECT id, name, street1, street2, city, state, zip FROM ftgo.restaurants;

INSERT INTO ftgo_restaurant_db.restaurant_menu_items (restaurant_id, id, name, price)
SELECT restaurant_id, id, name, price FROM ftgo.restaurant_menu_items;

-- Order data
INSERT INTO ftgo_order_db.orders (
    id, version, order_state, consumer_id, restaurant_id,
    delivery_address_street1, delivery_address_street2, delivery_address_city,
    delivery_address_state, delivery_address_zip,
    delivery_time, payment_token, order_minimum, ready_by, accept_time,
    preparing_time, ready_for_pickup_time, picked_up_time, delivered_time,
    assigned_courier_id
)
SELECT
    id, version, order_state, consumer_id, restaurant_id,
    delivery_address_street1, delivery_address_street2, delivery_address_city,
    delivery_address_state, delivery_address_zip,
    delivery_time, payment_token, order_minimum, ready_by, accept_time,
    preparing_time, ready_for_pickup_time, picked_up_time, delivered_time,
    assigned_courier_id
FROM ftgo.orders;

INSERT INTO ftgo_order_db.order_line_items (order_id, menu_item_id, name, price, quantity)
SELECT order_id, menu_item_id, name, price, quantity FROM ftgo.order_line_items;

-- Courier data
INSERT INTO ftgo_courier_db.courier (id, first_name, last_name, street1, street2, city, state, zip, available)
SELECT id, first_name, last_name, street1, street2, city, state, zip, available FROM ftgo.courier;

INSERT INTO ftgo_courier_db.courier_actions (courier_id, type, action_time, order_id)
SELECT courier_id, type, action_time, order_id FROM ftgo.courier_actions;
```

#### Step 3.4: Reset AUTO_INCREMENT Sequences

```sql
-- Ensure AUTO_INCREMENT starts after the max existing ID
SELECT CONCAT('ALTER TABLE ftgo_consumer_db.consumers AUTO_INCREMENT = ', MAX(id) + 1, ';')
FROM ftgo_consumer_db.consumers;
-- Execute the generated ALTER TABLE statement

-- Repeat for: ftgo_restaurant_db.restaurants, ftgo_order_db.orders, ftgo_courier_db.courier
```

#### Step 3.5: Verify Data

```sql
-- Row count comparison
SELECT 'consumers' AS tbl,
       (SELECT COUNT(*) FROM ftgo.consumers) AS monolith,
       (SELECT COUNT(*) FROM ftgo_consumer_db.consumers) AS per_service;

SELECT 'restaurants' AS tbl,
       (SELECT COUNT(*) FROM ftgo.restaurants) AS monolith,
       (SELECT COUNT(*) FROM ftgo_restaurant_db.restaurants) AS per_service;

SELECT 'orders' AS tbl,
       (SELECT COUNT(*) FROM ftgo.orders) AS monolith,
       (SELECT COUNT(*) FROM ftgo_order_db.orders) AS per_service;

SELECT 'courier' AS tbl,
       (SELECT COUNT(*) FROM ftgo.courier) AS monolith,
       (SELECT COUNT(*) FROM ftgo_courier_db.courier) AS per_service;
```

#### Step 3.6: Restart Services

```bash
systemctl start ftgo-consumer-service
systemctl start ftgo-restaurant-service
systemctl start ftgo-order-service
systemctl start ftgo-courier-service
```

### Phase 4: Decommission Shared Database

**Objective:** Remove the shared `ftgo` database once all services are
stable on their own databases.

**Wait period:** Minimum 2 weeks of stable operation on per-service
databases before decommissioning.

1. Keep the shared database in read-only mode for 2 weeks as a safety net.
2. Verify no service is connecting to the shared database (check MySQL
   `PROCESSLIST`).
3. Take a final backup.
4. Drop the shared database.

```sql
-- Final backup
-- mysqldump -u root -p ftgo > ftgo_final_backup_$(date +%Y%m%d).sql

-- Revoke shared access
REVOKE ALL PRIVILEGES ON ftgo.* FROM 'ftgo_consumer'@'%';
REVOKE ALL PRIVILEGES ON ftgo.* FROM 'ftgo_restaurant'@'%';
REVOKE ALL PRIVILEGES ON ftgo.* FROM 'ftgo_order'@'%';
REVOKE ALL PRIVILEGES ON ftgo.* FROM 'ftgo_courier'@'%';

-- Drop (only after verification period)
-- DROP DATABASE ftgo;
```

## 4. Rollback Procedures

See [Rollback Strategy](rollback-strategy.md) for detailed rollback
instructions at each phase.

### Quick Rollback Summary

| Phase | Rollback Action                                  | Data Loss Risk |
|-------|--------------------------------------------------|----------------|
| 1     | Drop per-service databases                       | None           |
| 2     | Drop tables, clear Flyway history                | None           |
| 3     | Reconfigure services to shared DB, restart       | Low (dual-write window) |
| 4     | Restore shared DB from backup, reconfigure       | Medium         |

## 5. Monitoring During Migration

### Key Metrics to Watch

| Metric                          | Source               | Alert Threshold       |
|---------------------------------|----------------------|-----------------------|
| Service health check            | `/actuator/health`   | Any DOWN              |
| Database connection pool usage  | Micrometer           | > 80% utilization     |
| Flyway migration status         | Application logs     | Any FAILED migration  |
| Order creation latency          | Prometheus           | > 2x baseline         |
| Error rate (5xx)                | API Gateway          | > 1% of requests      |

### Log Queries

```bash
# Check for Flyway migration errors
grep -i "flyway" /var/log/ftgo-*/application.log | grep -i "error\|fail"

# Check for database connection issues
grep -i "connection\|datasource" /var/log/ftgo-*/application.log | grep -i "error\|fail"
```

## 6. Contacts

| Role                 | Responsibility                              |
|----------------------|---------------------------------------------|
| DBA                  | Database provisioning, backups, permissions  |
| Platform Engineering | Service configuration, deployment            |
| SRE                  | Monitoring, incident response               |
| Product Owner        | Maintenance window approval                 |
