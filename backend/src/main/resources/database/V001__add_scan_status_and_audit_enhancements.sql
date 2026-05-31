-- V001__add_scan_status_and_audit_enhancements.sql
-- Adds malware scan status and enhanced audit logging

-- Add scan status columns to file_metadata
ALTER TABLE IF EXISTS file_metadata ADD COLUMN IF NOT EXISTS scan_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE IF EXISTS file_metadata ADD COLUMN IF NOT EXISTS scanned_at TIMESTAMP;
ALTER TABLE IF EXISTS file_metadata ADD COLUMN IF NOT EXISTS scan_details TEXT;

-- Add master key version for key rotation support
ALTER TABLE IF EXISTS file_metadata ADD COLUMN IF NOT EXISTS master_key_version INTEGER DEFAULT 1;

-- Add wrapped encryption key column
ALTER TABLE IF EXISTS file_metadata ADD COLUMN IF NOT EXISTS wrapped_encryption_key VARCHAR(500);

-- Add enhanced audit columns to audit_logs
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS user_agent TEXT;
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'SUCCESS';
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS failure_reason TEXT;
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS resource_id BIGINT;
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS resource_type VARCHAR(50);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_files_scan_status ON file_metadata(scan_status);
CREATE INDEX IF NOT EXISTS idx_files_user_id ON file_metadata(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_id ON audit_logs(resource_id, resource_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);

-- Add scan status constraint
ALTER TABLE file_metadata ADD CONSTRAINT chk_scan_status 
    CHECK (scan_status IN ('PENDING', 'SAFE', 'UNSAFE'));

-- Add audit status constraint
ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_status 
    CHECK (status IN ('SUCCESS', 'FAILED', 'UNAUTHORIZED'));

COMMENT ON COLUMN file_metadata.scan_status IS 'Malware scan status: PENDING (not scanned), SAFE (clean), UNSAFE (malicious)';
COMMENT ON COLUMN file_metadata.scanned_at IS 'Timestamp when file was last scanned for malware';
COMMENT ON COLUMN file_metadata.scan_details IS 'Detailed scan results or error messages from antivirus/security service';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address of client making the request (for security analysis)';
COMMENT ON COLUMN audit_logs.user_agent IS 'User-Agent string from client request (for forensics)';
COMMENT ON COLUMN audit_logs.status IS 'Status of the operation: SUCCESS, FAILED, UNAUTHORIZED';
COMMENT ON COLUMN audit_logs.failure_reason IS 'Reason for operation failure (validation error, authorization failure, etc)';
COMMENT ON COLUMN audit_logs.resource_id IS 'ID of the resource being accessed (file ID, folder ID, etc)';
COMMENT ON COLUMN audit_logs.resource_type IS 'Type of resource being accessed (FILE, FOLDER, USER, etc)';
