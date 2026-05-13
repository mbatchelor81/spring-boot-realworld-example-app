-- =============================================================================
-- ftgo-courier-service schema
-- Owner: ftgo-courier-service
-- Tables: courier, courier_actions
--
-- The courier_actions.order_id column references an order owned by
-- ftgo-order-service. No foreign key is defined — referential integrity
-- is maintained via domain events and eventual consistency.
-- =============================================================================

CREATE TABLE courier (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    street1     VARCHAR(255),
    street2     VARCHAR(255),
    city        VARCHAR(255),
    state       VARCHAR(255),
    zip         VARCHAR(20),
    available   BOOLEAN DEFAULT FALSE
);

CREATE TABLE courier_actions (
    courier_id  BIGINT NOT NULL,
    type        VARCHAR(30) NOT NULL,
    action_time TIMESTAMP,
    -- Reference to ftgo-order-service (no FK)
    order_id    BIGINT NOT NULL,
    CONSTRAINT fk_actions_courier
        FOREIGN KEY (courier_id) REFERENCES courier(id)
);

CREATE INDEX idx_actions_courier ON courier_actions(courier_id);
CREATE INDEX idx_actions_order   ON courier_actions(order_id);
