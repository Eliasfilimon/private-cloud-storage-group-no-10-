-- Database Schema for Secure Cloud Storage System
-- PostgreSQL 14+

-- Create database (run separately if needed)
-- CREATE DATABASE secure_cloud_storage;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'LECTURER')),
    department VARCHAR(100),
    storage_quota BIGINT NOT NULL DEFAULT 5368709120, -- 5GB in bytes
    storage_used BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- File metadata table
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    is_encrypted BOOLEAN NOT NULL DEFAULT TRUE,
    encryption_key VARCHAR(255),
    checksum VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 1,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id BIGINT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    status VARCHAR(20) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Shared files table
CREATE TABLE IF NOT EXISTS shared_files (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    owner_username VARCHAR(50) NOT NULL,
    shared_with_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shared_with_username VARCHAR(50) NOT NULL,
    permission VARCHAR(20) NOT NULL CHECK (permission IN ('VIEW', 'DOWNLOAD', 'EDIT')) DEFAULT 'VIEW',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    UNIQUE(file_id, shared_with_id)
);

-- Backup records table
CREATE TABLE IF NOT EXISTS backup_records (
    id BIGSERIAL PRIMARY KEY,
    backup_name VARCHAR(255) NOT NULL,
    backup_path VARCHAR(500) NOT NULL,
    backup_type VARCHAR(50) NOT NULL,
    file_count INTEGER NOT NULL DEFAULT 0,
    total_size BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'FAILED')),
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    created_by BIGINT REFERENCES users(id) ON DELETE SET NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_files_user_id ON file_metadata(user_id);
CREATE INDEX IF NOT EXISTS idx_files_deleted ON file_metadata(is_deleted);
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_shared_files_owner ON shared_files(owner_id);
CREATE INDEX IF NOT EXISTS idx_shared_files_shared_with ON shared_files(shared_with_id);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_files_updated_at BEFORE UPDATE ON file_metadata
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert default admin user (password: Admin@123)
-- Password hash generated using BCrypt with strength 12
INSERT INTO users (username, email, password, full_name, role, department, storage_quota, is_active)
VALUES (
    'admin',
    'admin@udom.ac.tz',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIRjYz7iei',
    'System Administrator',
    'ADMIN',
    'IT Department',
    10737418240, -- 10GB in bytes
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Sample lecturer user (password: Lecturer@123)
INSERT INTO users (username, email, password, full_name, role, department, storage_quota, is_active)
VALUES (
    'lecturer1',
    'lecturer1@udom.ac.tz',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Dr. John Doe',
    'LECTURER',
    'Computer Science',
    5368709120, -- 5GB in bytes
    TRUE
) ON CONFLICT (username) DO NOTHING;
