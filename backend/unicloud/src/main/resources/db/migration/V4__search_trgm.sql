-- =============================================================================
-- V01__search_trgm.sql – UC-31: pg_trgm tuning for fuzzy Cyrillic search
-- =============================================================================
-- Note: pg_trgm extension and GIN indexes on courses/materials/tags are already
-- created in V1__init_schema.sql. This migration adds the missing index on
-- users.full_name (needed for ?teacher= filter) and lowers the similarity
-- threshold to 0.2 so single-character Cyrillic typos are matched.

-- GIN index on teacher full name (used by SearchSpecification.teacherMatches)
CREATE INDEX IF NOT EXISTS idx_users_full_name_trgm
    ON users USING gin (full_name gin_trgm_ops);

-- Lower similarity threshold: default 0.3 is too strict for short Cyrillic words;
-- 0.2 catches one-character typos reliably while keeping GIN index usage.
ALTER DATABASE unicloud SET pg_trgm.similarity_threshold = 0.2;

