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
    first_name VARCHAR(100) DEFAULT NULL,
    last_name VARCHAR(100) DEFAULT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'STAFF')),
    department VARCHAR(100),
    storage_quota BIGINT NOT NULL DEFAULT 5368709120, -- 5GB in bytes
    storage_used BIGINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    totp_secret VARCHAR(255),
    totp_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    must_change_password BOOLEAN DEFAULT FALSE NOT NULL
);

-- Folder table (for Google Drive-like folder structure)
CREATE TABLE IF NOT EXISTS folders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    folder_name VARCHAR(255) NOT NULL,
    parent_folder_id BIGINT REFERENCES folders(id) ON DELETE CASCADE,
    folder_color VARCHAR(7) DEFAULT '#4A90E2',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, folder_name, parent_folder_id)
);

-- File metadata table
CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    folder_id BIGINT REFERENCES folders(id) ON DELETE SET NULL,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL UNIQUE,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT TRUE,
    wrapped_encryption_key VARCHAR(500),
    authentication_tag VARCHAR(50),
    master_key_version INTEGER NOT NULL DEFAULT 1,
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
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    backup_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED')),
    message TEXT,
    user_count INTEGER,
    file_count INTEGER,
    audit_log_count INTEGER,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Share links table (public/password-protected file links)
CREATE TABLE IF NOT EXISTS share_links (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(64) UNIQUE NOT NULL,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP,
    download_limit INTEGER,
    download_count INTEGER NOT NULL DEFAULT 0,
    password_hash VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- File versions table (version history for files)
CREATE TABLE IF NOT EXISTS file_versions (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    minio_object_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    wrapped_encryption_key VARCHAR(500),
    master_key_version INTEGER NOT NULL DEFAULT 1,
    created_by_username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_folders_user_id ON folders(user_id);
CREATE INDEX IF NOT EXISTS idx_folders_parent_id ON folders(parent_folder_id);
CREATE INDEX IF NOT EXISTS idx_files_user_id ON file_metadata(user_id);
CREATE INDEX IF NOT EXISTS idx_files_folder_id ON file_metadata(folder_id);
CREATE INDEX IF NOT EXISTS idx_files_deleted ON file_metadata(is_deleted);
CREATE INDEX IF NOT EXISTS idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_shared_files_owner ON shared_files(owner_id);
CREATE INDEX IF NOT EXISTS idx_shared_files_shared_with ON shared_files(shared_with_id);
CREATE INDEX IF NOT EXISTS idx_share_links_token ON share_links(token);
CREATE INDEX IF NOT EXISTS idx_share_links_file_id ON share_links(file_id);
CREATE INDEX IF NOT EXISTS idx_file_versions_file_id ON file_versions(file_id);
CREATE INDEX IF NOT EXISTS idx_backup_records_status ON backup_records(status);
CREATE INDEX IF NOT EXISTS idx_backup_records_created_at ON backup_records(created_at);

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
INSERT INTO users (username, email, password, first_name, last_name, full_name, role, department, storage_quota, is_active)
VALUES (
    'admin@udom.ac.tz',
    'admin@udom.ac.tz',
    '$2a$12$P7rT07Yk1.txHwzoURfAouoRUMiO1lkK6/Plz479erGOr5wjidTJC',
    'System',
    'Administrator',
    'System Administrator',
    'ADMIN',
    'IT Department',
    10737418240, -- 10GB in bytes
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Insert default staff user (password: Staff@123)
INSERT INTO users (username, email, password, first_name, last_name, full_name, role, department, storage_quota, is_active)
VALUES (
    'staff@udom.ac.tz',
    'staff@udom.ac.tz',
    '$2a$12$4aRL7R/d3rkFsAUTJqvnOud9ogxUWxYTGSGVnHlhpg4f7Nh3sxWZ2',
    'Staff',
    'User',
    'Staff User',
    'STAFF',
    'General Department',
    5368709120, -- 5GB in bytes
    TRUE
) ON CONFLICT (username) DO NOTHING;

-- Sessions table for server-side session management
CREATE TABLE IF NOT EXISTS sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(1000) NOT NULL UNIQUE,
    refresh_token VARCHAR(1000) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes for sessions table
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_token ON sessions(token);
CREATE INDEX IF NOT EXISTS idx_sessions_is_active ON sessions(is_active);

