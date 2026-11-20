ALTER TABLE users
    ADD COLUMN external_subject VARCHAR(255);

CREATE UNIQUE INDEX uk_users_external_subject
    ON users (external_subject);

ALTER TABLE users
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE courses
    ADD COLUMN external_course_offering_id BIGINT;

CREATE UNIQUE INDEX uk_courses_external_offering
    ON courses (external_course_offering_id);

CREATE TABLE integration_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    user_alias VARCHAR(255) NOT NULL,
    course_offering_id BIGINT,
    material_id UUID,
    material_version INT,
    payload JSONB NOT NULL,
    delivered_at TIMESTAMPTZ,
    next_retry_at TIMESTAMPTZ,
    retry_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_error TEXT,
    CONSTRAINT uk_integration_events_event_id UNIQUE (event_id),
    CONSTRAINT chk_integration_events_type
        CHECK (event_type IN ('material.viewed', 'material.read', 'material.unread', 'material.uploaded')),
    CONSTRAINT chk_integration_events_status
        CHECK (status IN ('PENDING', 'DELIVERED', 'FAILED')),
    CONSTRAINT chk_integration_events_retry_count
        CHECK (retry_count BETWEEN 0 AND 3)
);

CREATE INDEX idx_integration_events_retry
    ON integration_events (status, next_retry_at);
