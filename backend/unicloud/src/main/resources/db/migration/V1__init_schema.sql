-- =============================================================================
-- V1__init_schema.sql – UniCloud Database Initial Schema
-- Based on docs/DATA_MODEL.md (UC-27, UC-31, UC-41, UC-42)
-- =============================================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- -----------------------------------------------------------------------------
-- 1. Table: users
-- -----------------------------------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN', 'SERVICE'))
);

-- -----------------------------------------------------------------------------
-- 2. Table: courses
-- -----------------------------------------------------------------------------
CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    discipline VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    description TEXT,
    teacher_id UUID NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uk_courses_code UNIQUE (code),
    CONSTRAINT fk_courses_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_courses_teacher_id ON courses (teacher_id);
CREATE INDEX idx_courses_discipline_semester ON courses (discipline, semester);
CREATE INDEX idx_courses_title_trgm ON courses USING gin (title gin_trgm_ops);
CREATE INDEX idx_courses_discipline_trgm ON courses USING gin (discipline gin_trgm_ops);

-- -----------------------------------------------------------------------------
-- 3. Table: modules
-- -----------------------------------------------------------------------------
CREATE TABLE modules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_modules_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

CREATE INDEX idx_modules_course_order ON modules (course_id, order_index ASC);

-- -----------------------------------------------------------------------------
-- 4. Table: lessons
-- -----------------------------------------------------------------------------
CREATE TABLE lessons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    order_index INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_lessons_module FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

CREATE INDEX idx_lessons_module_order ON lessons (module_id, order_index ASC);

-- -----------------------------------------------------------------------------
-- 5. Table: materials
-- -----------------------------------------------------------------------------
CREATE TABLE materials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    video_url VARCHAR(1000),
    current_version INT NOT NULL DEFAULT 1,
    is_hidden BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_materials_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT chk_materials_type CHECK (type IN ('NOTES', 'PRESENTATION', 'VIDEO_LINK', 'ASSIGNMENT', 'LITERATURE'))
);

CREATE INDEX idx_materials_lesson_hidden ON materials (lesson_id, is_hidden);
CREATE INDEX idx_materials_type ON materials (type);
CREATE INDEX idx_materials_title_trgm ON materials USING gin (title gin_trgm_ops);
CREATE INDEX idx_materials_description_trgm ON materials USING gin (description gin_trgm_ops);

-- -----------------------------------------------------------------------------
-- 6. Table: material_versions
-- -----------------------------------------------------------------------------
CREATE TABLE material_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    material_id UUID NOT NULL,
    version_number INT NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    file_hash VARCHAR(64),
    uploaded_by UUID NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_material_versions_material FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE,
    CONSTRAINT fk_material_versions_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id),
    CONSTRAINT uk_material_version UNIQUE (material_id, version_number)
);

CREATE INDEX idx_material_versions_lookup ON material_versions (material_id, version_number DESC);

-- -----------------------------------------------------------------------------
-- 7. Table: tags & material_tags
-- -----------------------------------------------------------------------------
CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_tags_name UNIQUE (name)
);

CREATE INDEX idx_tags_name_trgm ON tags USING gin (name gin_trgm_ops);

CREATE TABLE material_tags (
    material_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    PRIMARY KEY (material_id, tag_id),
    CONSTRAINT fk_material_tags_material FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE,
    CONSTRAINT fk_material_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_material_tags_tag_id ON material_tags (tag_id);

-- -----------------------------------------------------------------------------
-- 8. Table: read_marks
-- -----------------------------------------------------------------------------
CREATE TABLE read_marks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    material_id UUID NOT NULL,
    version_at_read INT NOT NULL DEFAULT 1,
    read_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT fk_read_marks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_read_marks_material FOREIGN KEY (material_id) REFERENCES materials(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_material_read UNIQUE (user_id, material_id)
);

CREATE INDEX idx_read_marks_user_id ON read_marks (user_id);

