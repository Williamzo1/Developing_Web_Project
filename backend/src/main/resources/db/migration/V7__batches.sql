CREATE TABLE batches (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    code VARCHAR(120) NOT NULL,
    public_code UUID NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('IN_STOCK', 'MADE_TO_ORDER')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit VARCHAR(40) NOT NULL,
    reference_price NUMERIC(15,2) CHECK (reference_price >= 0),
    price_visible BOOLEAN NOT NULL DEFAULT false,
    supply_capacity VARCHAR(200),
    available_from DATE,
    visibility VARCHAR(7) NOT NULL CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    share_token_hash CHAR(64),
    partner_note TEXT,
    hidden BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (product_id, code)
);

CREATE INDEX batches_product_id_idx ON batches(product_id);

CREATE TABLE batch_milestones (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES batches(id),
    title VARCHAR(160) NOT NULL,
    note TEXT,
    happened_on DATE NOT NULL
);
CREATE INDEX batch_milestones_batch_idx ON batch_milestones(batch_id, happened_on, id);
