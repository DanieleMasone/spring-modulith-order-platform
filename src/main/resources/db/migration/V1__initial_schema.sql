CREATE TABLE customers (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    full_name VARCHAR(160) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE price_catalog (
    product_code VARCHAR(64) PRIMARY KEY,
    description VARCHAR(200) NOT NULL,
    unit_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL
);

CREATE INDEX orders_customer_id_idx ON orders (customer_id);

CREATE TABLE order_lines (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_code VARCHAR(64) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL
);

CREATE INDEX order_lines_order_id_idx ON order_lines (order_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    authorized_at TIMESTAMPTZ,
    version BIGINT NOT NULL
);

CREATE INDEX payments_status_idx ON payments (status);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient VARCHAR(160) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    type VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX notifications_created_at_idx ON notifications (created_at DESC);
