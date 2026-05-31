-- Migration: Add Indexes and Constraints
-- Date: 2024-01-24
-- Description: Add additional indexes and constraints for performance and data integrity

-- Add unique constraint on username (already in V1, but ensuring it exists)
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS uk_users_username UNIQUE (username);

-- Add unique constraint on email (already in V1, but ensuring it exists)
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS uk_users_email UNIQUE (email);

-- Add check constraint for storage quota
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_users_storage_quota CHECK (storage_quota >= 0);

-- Add check constraint for storage used
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_users_storage_used CHECK (storage_used >= 0);

-- Add check constraint for storage used <= storage quota
ALTER TABLE users ADD CONSTRAINT IF NOT EXISTS chk_users_storage_usage CHECK (storage_used <= storage_quota);

-- Add index on users role
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Add index on users is_active
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users(is_active);

-- Add index on users department
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department);

-- Add index on file_metadata mime_type
CREATE INDEX IF NOT EXISTS idx_file_metadata_mime_type ON file_metadata(mime_type);

-- Add index on file_metadata file_size
CREATE INDEX IF NOT EXISTS idx_file_metadata_file_size ON file_metadata(file_size);

-- Add index on share_links created_by
CREATE INDEX IF NOT EXISTS idx_share_links_created_by ON share_links(created_by);

-- Add index on share_links expires_at
CREATE INDEX IF NOT EXISTS idx_share_links_expires_at ON share_links(expires_at);

-- Add index on share_links is_active
CREATE INDEX IF NOT EXISTS idx_share_links_is_active ON share_links(is_active);

-- Add index on audit_log resource_type
CREATE INDEX IF NOT EXISTS idx_audit_log_resource_type ON audit_log(resource_type);

-- Add index on audit_log status
CREATE INDEX IF NOT EXISTS idx_audit_log_status ON audit_log(status);

-- Add index on audit_log ip_address
CREATE INDEX IF NOT EXISTS idx_audit_log_ip_address ON audit_log(ip_address);
