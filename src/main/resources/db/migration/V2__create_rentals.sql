CREATE TYPE rental_status AS ENUM ('active', 'completed', 'cancelled');

CREATE TABLE rentals (
    id            BIGSERIAL      PRIMARY KEY,
    customer_id   BIGINT         NOT NULL,
    equipment_id  BIGINT         NOT NULL,
    start_date    DATE           NOT NULL,
    end_date      DATE           NOT NULL,
    total_price   NUMERIC(10,2)  NOT NULL,
    status        rental_status  NOT NULL DEFAULT 'active',
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT rentals_customer_fk
        FOREIGN KEY (customer_id)  REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT rentals_equipment_fk
        FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,

    CONSTRAINT rentals_dates_order
        CHECK (end_date > start_date),
    CONSTRAINT rentals_total_price_positive
        CHECK (total_price > 0)
);

CREATE INDEX idx_rentals_customer_id  ON rentals (customer_id);
CREATE INDEX idx_rentals_equipment_id ON rentals (equipment_id);
CREATE INDEX idx_rentals_status       ON rentals (status);
CREATE INDEX idx_rentals_created_at   ON rentals (created_at DESC);