-- Initial Database Schema for Secure Cloud Storage System
-- PostgreSQL 14+
-- Date: 2024-01-24

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    department VARCHAR(100),
    storage_quota BIGINT NOT NULL DEFAULT 5368709120, -- 5GB in bytes
    storage_used BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    two_factor_backup_codes TEXT,
    last_login TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- File metadata table
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100),
    file_size BIGINT NOT NULL,
    file_key TEXT, -- Encrypted file key
    key_version INTEGER DEFAULT 1,
    checksum VARCHAR(64),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- File versions table
CREATE TABLE IF NOT EXISTS file_versions (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_key TEXT,
    checksum VARCHAR(64),
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(file_id, version_number)
);

-- Share links table
CREATE TABLE IF NOT EXISTS share_links (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    share_token VARCHAR(255) UNIQUE NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP,
    max_downloads INTEGER,
    download_count INTEGER DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_file_metadata_user_id ON file_metadata(user_id);
CREATE INDEX IF NOT EXISTS idx_file_metadata_is_deleted ON file_metadata(is_deleted);
CREATE INDEX IF NOT EXISTS idx_file_metadata_created_at ON file_metadata(created_at);
CREATE INDEX IF NOT EXISTS idx_file_versions_file_id ON file_versions(file_id);
CREATE INDEX IF NOT EXISTS idx_share_links_file_id ON share_links(file_id);
CREATE INDEX IF NOT EXISTS idx_share_links_token ON share_links(share_token);
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);

-- Insert default admin user (password: ADMINISTRATOR - from last name)
INSERT INTO users (username, email, password, first_name, last_name, full_name, role, department, storage_quota, is_active, must_change_password)
VALUES ('admin@udom.ac.tz', 'admin@udom.ac.tz', '$2a$10$slYQmyNdGzDq8CTtW2e.Be07DYY4d7aNK2PSUmCD/YBST12dW2JNm', 'System', 'Administrator', 'System Administrator', 'ADMIN', 'IT', 10737418240, TRUE, TRUE)
ON CONFLICT (username) DO NOTHING;
