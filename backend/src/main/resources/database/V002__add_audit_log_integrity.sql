-- V002__add_audit_log_integrity.sql (Optional)
-- Adds HMAC integrity verification for audit logs
-- This is optional and can be used for audit log tamper detection

ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS log_hmac VARCHAR(256);
ALTER TABLE IF EXISTS audit_logs ADD COLUMN IF NOT EXISTS integrity_verified BOOLEAN DEFAULT FALSE;

-- Create index for integrity verification
CREATE INDEX IF NOT EXISTS idx_audit_logs_integrity ON audit_logs(integrity_verified);

COMMENT ON COLUMN audit_logs.log_hmac IS 'HMAC signature of log entry for tamper detection';
COMMENT ON COLUMN audit_logs.integrity_verified IS 'Whether this log entry has been verified to not be tampered with';
