-- =============================================================================
-- ftgo-restaurant-service schema
-- Owner: ftgo-restaurant-service
-- Tables: restaurants, restaurant_menu_items
-- =============================================================================

CREATE TABLE restaurants (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    street1  VARCHAR(255),
    street2  VARCHAR(255),
    city     VARCHAR(255),
    state    VARCHAR(255),
    zip      VARCHAR(20)
);

CREATE TABLE restaurant_menu_items (
    restaurant_id  BIGINT NOT NULL,
    id             VARCHAR(255) NOT NULL,
    name           VARCHAR(255) NOT NULL,
    price          DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
);

CREATE INDEX idx_menu_items_restaurant ON restaurant_menu_items(restaurant_id);
