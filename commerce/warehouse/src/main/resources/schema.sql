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