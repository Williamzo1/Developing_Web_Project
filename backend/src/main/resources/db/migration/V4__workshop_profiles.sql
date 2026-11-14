ALTER TABLE workshops
    ADD COLUMN description TEXT,
    ADD COLUMN family_history TEXT,
    ADD COLUMN address VARCHAR(300),
    ADD COLUMN phone VARCHAR(40),
    ADD COLUMN contact_email VARCHAR(254),
    ADD COLUMN facebook_url VARCHAR(500),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION,
    ADD CONSTRAINT workshop_coordinates_check CHECK (
        (latitude IS NULL AND longitude IS NULL) OR
        (latitude IS NOT NULL AND longitude IS NOT NULL
            AND latitude BETWEEN -90 AND 90 AND longitude BETWEEN -180 AND 180)
    );
