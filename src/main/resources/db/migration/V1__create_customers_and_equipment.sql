CREATE EXTENSION IF NOT EXISTS citext;

CREATE TYPE equipment_status AS ENUM ('available', 'maintenance', 'retired');

CREATE TABLE customers (
    id          BIGSERIAL    PRIMARY KEY,
    email       CITEXT       NOT NULL UNIQUE,
    full_name   VARCHAR(255) NOT NULL,
    phone       VARCHAR(32)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT customers_email_format
        CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),
    CONSTRAINT customers_full_name_not_empty
        CHECK (length(trim(full_name)) > 0),
    CONSTRAINT customers_phone_format
        CHECK (phone ~ '^\+?[0-9 ()\-]{7,32}$')
);

CREATE TABLE equipment (
    id           BIGSERIAL         PRIMARY KEY,
    name         VARCHAR(255)      NOT NULL,
    category     VARCHAR(64)       NOT NULL,
    daily_price  NUMERIC(10,2)     NOT NULL,
    status       equipment_status  NOT NULL DEFAULT 'available',
    created_at   TIMESTAMPTZ       NOT NULL DEFAULT now(),
    CONSTRAINT equipment_name_not_empty
        CHECK (length(trim(name)) > 0),
    CONSTRAINT equipment_daily_price_positive
        CHECK (daily_price > 0)
);