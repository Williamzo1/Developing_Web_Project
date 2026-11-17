ALTER TABLE workshops ADD COLUMN approved BOOLEAN NOT NULL DEFAULT TRUE;

CREATE TABLE accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE CHECK (username ~ '^[a-z0-9-]{3,60}$'),
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(12) NOT NULL CHECK (role IN ('BUYER', 'ARTISAN', 'ADMIN')),
    workshop_id BIGINT UNIQUE REFERENCES workshops(id),
    CHECK ((role = 'ARTISAN') = (workshop_id IS NOT NULL))
);

CREATE TABLE account_sessions (
    token_hash CHAR(64) PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX account_sessions_by_expiry ON account_sessions(expires_at);
