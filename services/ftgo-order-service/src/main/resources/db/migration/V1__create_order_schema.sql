-- =============================================================================
-- ftgo-order-service schema
-- Owner: ftgo-order-service
-- Tables: orders, order_line_items
--
-- Cross-service references (consumer_id, restaurant_id, assigned_courier_id)
-- are stored as plain BIGINT columns — no foreign keys to external service
-- tables. Referential integrity across services is maintained via eventual
-- consistency and domain events.
-- =============================================================================

CREATE TABLE orders (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    version               BIGINT,
    order_state           VARCHAR(30) NOT NULL,

    -- Reference to ftgo-consumer-service (no FK)
    consumer_id           BIGINT NOT NULL,

    -- Reference to ftgo-restaurant-service (no FK)
    restaurant_id         BIGINT NOT NULL,

    -- Embedded DeliveryInformation (Address via @AttributeOverride)
    delivery_address_street1 VARCHAR(255),
    delivery_address_street2 VARCHAR(255),
    delivery_address_city    VARCHAR(255),
    delivery_address_state   VARCHAR(255),
    delivery_address_zip     VARCHAR(20),
    delivery_time         TIMESTAMP,

    -- Embedded PaymentInformation
    payment_token         VARCHAR(255),

    -- Embedded Money (order minimum)
    order_minimum         DECIMAL(19,2),

    -- Timestamps
    ready_by              TIMESTAMP,
    accept_time           TIMESTAMP,
    preparing_time        TIMESTAMP,
    ready_for_pickup_time TIMESTAMP,
    picked_up_time        TIMESTAMP,
    delivered_time        TIMESTAMP,

    -- Reference to ftgo-courier-service (no FK)
    assigned_courier_id   BIGINT
);

CREATE INDEX idx_orders_consumer    ON orders(consumer_id);
CREATE INDEX idx_orders_restaurant  ON orders(restaurant_id);
CREATE INDEX idx_orders_courier     ON orders(assigned_courier_id);
CREATE INDEX idx_orders_state       ON orders(order_state);

CREATE TABLE order_line_items (
    order_id     BIGINT NOT NULL,
    menu_item_id VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    price        DECIMAL(19,2) NOT NULL,
    quantity     INT NOT NULL,
    CONSTRAINT fk_line_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_line_items_order ON order_line_items(order_id);
