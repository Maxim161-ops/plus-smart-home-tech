CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.products (
    product_id UUID PRIMARY KEY,
    quantity BIGINT NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    depth DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    fragile BOOLEAN NOT NULL,

    CONSTRAINT products_quantity_check
        CHECK (quantity >= 0),

    CONSTRAINT products_width_check
        CHECK (width >= 1),

    CONSTRAINT products_height_check
        CHECK (height >= 1),

    CONSTRAINT products_depth_check
        CHECK (depth >= 1),

    CONSTRAINT products_weight_check
        CHECK (weight >= 1)
);

CREATE TABLE IF NOT EXISTS warehouse.order_booking (
    booking_id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    delivery_id UUID
);

CREATE TABLE IF NOT EXISTS warehouse.order_booking_products (
    booking_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL,

    PRIMARY KEY (booking_id, product_id),

    CONSTRAINT fk_order_booking
        FOREIGN KEY (booking_id)
        REFERENCES warehouse.order_booking(booking_id)
        ON DELETE CASCADE,

    CONSTRAINT booking_quantity_check
        CHECK (quantity > 0)
);