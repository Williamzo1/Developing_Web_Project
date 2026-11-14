ALTER TABLE products
    ADD COLUMN reference_price NUMERIC(15,2) CHECK (reference_price >= 0),
    ADD COLUMN price_visible BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN hidden BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN admin_hidden BOOLEAN NOT NULL DEFAULT false;

CREATE INDEX products_workshop_id_idx ON products(workshop_id);
CREATE INDEX products_category_id_idx ON products(category_id);

CREATE TABLE process_steps (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    position INTEGER NOT NULL CHECK (position >= 0),
    name VARCHAR(160) NOT NULL,
    description TEXT,
    UNIQUE (product_id, position)
);
