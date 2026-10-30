-- нужен для оператора = на BIGINT внутри gist-индекса
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE rentals
    ADD CONSTRAINT rentals_no_overlap
    EXCLUDE USING gist (
        equipment_id WITH =,
        daterange(start_date, end_date, '[]') WITH &&
    )
    WHERE (status = 'active');