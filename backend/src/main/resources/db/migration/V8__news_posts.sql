CREATE TABLE news_posts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    author_account_id BIGINT NOT NULL REFERENCES accounts(id),
    slug VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(160) NOT NULL,
    body TEXT NOT NULL,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('MATERIAL_PRICE', 'POWER_CUT', 'POLICY', 'EVENT')),
    event_start TIMESTAMPTZ,
    event_end TIMESTAMPTZ,
    event_place VARCHAR(300),
    hidden BOOLEAN NOT NULL DEFAULT false,
    published_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (event_end IS NULL OR (event_start IS NOT NULL AND event_end >= event_start))
);
CREATE INDEX news_posts_public_idx ON news_posts(published_at DESC, id DESC) WHERE NOT hidden;
