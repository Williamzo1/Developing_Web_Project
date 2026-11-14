CREATE TABLE featured_content (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    workshop_id BIGINT REFERENCES workshops(id),
    product_id BIGINT REFERENCES products(id),
    batch_id BIGINT REFERENCES batches(id)
);
