CREATE SCHEMA IF NOT EXISTS shopping_store;

CREATE TABLE IF NOT EXISTS shopping_store.products
(
    product_id       UUID PRIMARY KEY,
    product_name     VARCHAR(255) NOT NULL,
    description      TEXT NOT NULL,
    image_src        VARCHAR(1000),
    quantity_state   VARCHAR(20) NOT NULL,
    product_state    VARCHAR(20) NOT NULL,
    product_category VARCHAR(20) NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,

    CONSTRAINT products_quantity_state_check
        CHECK (quantity_state IN ('ENDED', 'FEW', 'ENOUGH', 'MANY')),

    CONSTRAINT products_product_state_check
        CHECK (product_state IN ('ACTIVE', 'DEACTIVATE')),

    CONSTRAINT products_product_category_check
        CHECK (product_category IN ('LIGHTING', 'CONTROL', 'SENSORS')),

    CONSTRAINT products_price_check
        CHECK (price >= 1)
);