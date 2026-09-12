CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.orders (
    order_id UUID PRIMARY KEY,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,

    delivery_country VARCHAR(255),
    delivery_city VARCHAR(255),
    delivery_street VARCHAR(255),
    delivery_house VARCHAR(255),
    delivery_flat VARCHAR(255),

    state VARCHAR(50) NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price NUMERIC(19, 2),
    delivery_price NUMERIC(19, 2),
    product_price NUMERIC(19, 2)
);

CREATE TABLE IF NOT EXISTS orders.order_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,

    PRIMARY KEY (order_id, product_id),

    CONSTRAINT fk_order_products_order
        FOREIGN KEY (order_id)
        REFERENCES orders.orders(order_id)
        ON DELETE CASCADE,

    CONSTRAINT order_product_quantity_check
        CHECK (quantity > 0)
);