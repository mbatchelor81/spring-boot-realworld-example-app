-- =============================================================================
-- ftgo-consumer-service schema
-- Owner: ftgo-consumer-service
-- Tables: consumers
-- =============================================================================

CREATE TABLE consumers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255)
);
