CREATE TABLE quote_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    buyer_account_id BIGINT NOT NULL REFERENCES accounts(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    title VARCHAR(160) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit VARCHAR(40) NOT NULL,
    spec TEXT NOT NULL,
    desired_by DATE,
    note TEXT,
    bidding_deadline TIMESTAMPTZ,
    target_workshop_id BIGINT REFERENCES workshops(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'SENT', 'BIDDING', 'ACCEPTED', 'IN_PRODUCTION', 'DELIVERED', 'CANCELLED')),
    accepted_quote_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK ((status = 'DRAFT') = (target_workshop_id IS NULL)),
    CHECK ((status IN ('ACCEPTED', 'IN_PRODUCTION', 'DELIVERED')) = (accepted_quote_id IS NOT NULL))
);

CREATE TABLE quotes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES quote_requests(id),
    workshop_id BIGINT NOT NULL REFERENCES workshops(id),
    unit_price NUMERIC(15,2) NOT NULL CHECK (unit_price >= 0),
    total_price NUMERIC(15,2) NOT NULL CHECK (total_price >= 0),
    lead_time_days INTEGER NOT NULL CHECK (lead_time_days > 0),
    note TEXT,
    status VARCHAR(16) NOT NULL CHECK (status IN ('SUBMITTED', 'ACCEPTED', 'NOT_SELECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (request_id, workshop_id),
    UNIQUE (request_id, id)
);
CREATE UNIQUE INDEX quotes_one_accepted_idx ON quotes(request_id) WHERE status = 'ACCEPTED';
ALTER TABLE quote_requests ADD CONSTRAINT quote_requests_accepted_fk
    FOREIGN KEY (id, accepted_quote_id) REFERENCES quotes(request_id, id);

CREATE TABLE quote_request_events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id BIGINT NOT NULL REFERENCES quote_requests(id) ON DELETE CASCADE,
    actor_account_id BIGINT NOT NULL REFERENCES accounts(id),
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX quote_requests_buyer_idx ON quote_requests(buyer_account_id, id);
CREATE INDEX quote_requests_target_idx ON quote_requests(target_workshop_id, id);
CREATE INDEX quote_request_events_request_idx ON quote_request_events(request_id, id);
