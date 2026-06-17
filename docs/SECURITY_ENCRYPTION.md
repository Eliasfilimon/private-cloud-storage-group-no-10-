# Security Documentation

## Overview
This document outlines the security architecture, features, and best practices for the Secure Cloud Storage system.

## Table of Contents
1. [Authentication & Authorization](#authentication--authorization)
2. [Encryption](#encryption)
3. [API Security](#api-security)
4. [Infrastructure Security](#infrastructure-security)
5. [Data Protection](#data-protection)
6. [Incident Response](#incident-response)
7. [Security Checklist](#security-checklist)

---

## Authentication & Authorization

### User Authentication
- **Method**: JWT (JSON Web Tokens)
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Token Expiration**: Configured in `JwtProperties.java`
- **Refresh Tokens**: Implement refresh token mechanism for long-lived sessions

### Password Policy
- **Minimum Length**: 8 characters
- **Complexity**: Must include uppercase, lowercase, numbers, special characters
- **Default Password**: Generated from user's last name (uppercase) on account creation
- **Password Change**: Required on first login
- **Password Reset**: Admin can reset user passwords via `/api/admin/users/{userId}/reset-password`

### Two-Factor Authentication (2FA)
- **Method**: TOTP (Time-based One-Time Password)
- **Implementation**: `TotpService.java`
- **Backup Codes**: Generated during 2FA setup
- **Enforcement**: Optional but recommended for admin accounts

### Role-Based Access Control (RBAC)
- **Roles**: ADMIN, STAFF
- **Admin Permissions**:
  - User management (create, update, delete, reset password)
  - System health monitoring
  - Audit log access
  - Backup management
  - Storage request approval
- **Staff Permissions**:
  - File upload/download
  - File sharing
  - Storage quota management

### Authorization
- **Method**: Spring Security with `@PreAuthorize` annotations
- **Enforcement**: All endpoints require authentication except:
  - `/api/auth/login`
  - `/api/auth/register`
  - `/actuator/health`

---

## Encryption

### File Encryption
- **Algorithm**: AES-256-GCM (Advanced Encryption Standard with Galois/Counter Mode)
- **Key Size**: 256 bits (32 bytes)
- **IV Length**: 12 bytes (96 bits)
- **Authentication Tag**: 128 bits (16 bytes)
- **Implementation**: `FileEncryptionService.java`

### Encryption Process
1. Generate unique file encryption key (256-bit random)
2. Encrypt file data using AES-256-GCM
3. Wrap file key with master encryption key
4. Store encrypted file in MinIO
5. Store wrapped key and authentication tag in database

### Master Encryption Key
- **Location**: Environment variable `MASTER_ENCRYPTION_KEY`
- **Format**: Base64-encoded 256-bit key
- **Rotation**: Implement quarterly rotation with key versioning
- **Access**: Only accessible to backend service
- **⚠️ CRITICAL**: Never commit to version control

### Key Versioning
- **Current Version**: 1
- **Purpose**: Support key rotation without re-encrypting all files
- **Implementation**: Store `master_key_version` with each encrypted file
- **Future**: Implement automatic re-encryption with new keys

### Password Hashing
- **Algorithm**: BCrypt with strength 12
- **Implementation**: `PasswordEncoder` in `SecurityConfig.java`
- **Salting**: Automatic per BCrypt specification

---

## API Security

### CORS (Cross-Origin Resource Sharing)
- **Allowed Origins**: Configured in `docker-compose.yml`
- **Default**: `http://localhost:3002`, `http://localhost:3000`, `http://localhost:5173`
- **Production**: Update `CORS_ALLOWED_ORIGINS` environment variable
- **Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Credentials**: Allowed

### CSRF Protection
- **Status**: Disabled for JWT-based stateless API ✓
- **Reasoning**: JWT tokens are not vulnerable to CSRF
- **Note**: Ensure all state-changing operations use POST/PUT/DELETE

### Security Headers
- **X-Content-Type-Options**: `nosniff` (prevent MIME sniffing)
- **X-Frame-Options**: `DENY` (prevent clickjacking)
- **X-XSS-Protection**: `1; mode=block` (XSS protection)
- **Content-Security-Policy**: Configured to prevent inline scripts
- **Referrer-Policy**: `strict-origin-when-cross-origin`
- **⚠️ TODO**: Add `Permissions-Policy` header

### Rate Limiting
- **Status**: Implemented in `RateLimitingFilter.java`
- **Recommended Limits**:
  - Login: 5 attempts/minute per IP
  - Register: 3 attempts/hour per IP
  - Admin operations: 20 requests/minute per user
- **⚠️ TODO**: Verify filter is active and configured

### Input Validation
- **Method**: Spring Validation with `@Valid` annotations
- **File Names**: ⚠️ TODO - Add path traversal prevention
- **Folder Names**: ⚠️ TODO - Add special character validation
- **Email**: Validated with `EmailValidator.java`
- **Passwords**: Validated with `PasswordValidator.java`

---

## Infrastructure Security

### Docker Security
- **Base Images**: Alpine Linux (minimal attack surface)
- **Non-root User**: Backend runs as `appuser` (not root)
- **Network**: Isolated Docker network `scs_network`
- **Secrets**: Use environment variables, never hardcode

### Database Security
- **Type**: PostgreSQL 15
- **Credentials**: ⚠️ CRITICAL - Use strong passwords in `.env`
- **Default**: `postgres` / `postgres` - CHANGE IMMEDIATELY
- **Port**: 55432 (mapped from 5432)
- **Access**: Only accessible from backend service
- **Backups**: ⚠️ TODO - Implement encrypted backups

### MinIO Security
- **Credentials**: ⚠️ CRITICAL - Change default credentials
- **Default**: `minioadmin` / `minioadmin123` - CHANGE IMMEDIATELY
- **Port**: 9001 (console) - ⚠️ TODO - Restrict to internal network
- **Bucket**: `secure-cloud-storage`
- **Encryption**: Client-side encryption before upload
- **Access**: Only accessible from backend service

### Network Security
- **Firewall**: Configure to allow only necessary ports
- **Ports**:
  - 3002: Frontend (HTTP)
  - 8080: Backend API (HTTP)
  - 5432: PostgreSQL (internal only)
  - 9000/9001: MinIO (internal only)
- **⚠️ TODO**: Implement HTTPS/TLS for all services

### SSL/TLS Configuration
- **Status**: ⚠️ NOT IMPLEMENTED
- **Required for Production**: Yes
- **Certificate**: Use Let's Encrypt or your organization's CA
- **HSTS**: Enable with `max-age=31536000; includeSubDomains`
- **Implementation**: Configure in reverse proxy (Nginx, Apache) or Spring Boot

---

## Data Protection

### File Storage
- **Location**: MinIO object storage
- **Encryption**: AES-256-GCM (client-side before upload)
- **Integrity**: SHA-256 checksum stored with metadata
- **Versioning**: File versions tracked in `file_versions` table
- **Deletion**: Soft delete (marked as deleted, not removed)
- **Recovery**: 30-day recovery period for deleted files

### Audit Logging
- **Implementation**: `AuditLogService.java`
- **Logged Events**:
  - User login (success/failure)
  - Password changes
  - Admin actions (user creation, deletion, password reset)
  - File operations (upload, download, delete, share)
  - Authorization failures
- **Retention**: 1 year minimum
- **Access**: Admin only
- **⚠️ TODO**: Send critical logs to external SIEM

### Data Retention
- **User Data**: Retained until account deletion
- **Deleted Files**: 30-day recovery period
- **Audit Logs**: 1 year minimum
- **Backups**: 90 days
- **⚠️ TODO**: Implement automatic cleanup policies

### User Data Privacy
- **Encryption**: All files encrypted with user-specific keys
- **Isolation**: Users cannot access other users' files
- **Admin Access**: Admins cannot view file contents (encrypted)
- **Sharing**: Explicit permission required for file sharing
- **Deletion**: Users can delete their own files permanently

---

## Incident Response

### Security Incident Procedure
1. **Detection**: Monitor audit logs and alerts
2. **Assessment**: Determine scope and impact
3. **Containment**: Disable affected accounts if necessary
4. **Eradication**: Remove malicious access
5. **Recovery**: Restore from backups if needed
6. **Notification**: Notify affected users within 72 hours
7. **Documentation**: Log incident details for review

### Breach Notification
- **Timeline**: Notify users within 72 hours of discovery
- **Content**: What data was accessed, what actions to take
- **Channels**: Email to registered email address
- **Documentation**: Keep records for regulatory compliance

### Password Reset After Breach
- **Admin Action**: Force password change on next login
- **2FA**: Disable and require re-setup
- **Sessions**: Invalidate all existing sessions
- **Audit**: Log the incident

### Account Lockout
- **Trigger**: 5 failed login attempts
- **Duration**: 15 minutes
- **Notification**: User notified via email
- **Admin Override**: Admin can unlock account

---

## Security Checklist

### Before Production Deployment
- [ ] Change all default credentials (PostgreSQL, MinIO, Grafana)
- [ ] Move hardcoded secrets to `.env` file
- [ ] Generate unique `MASTER_ENCRYPTION_KEY`
- [ ] Configure HTTPS/TLS certificates
- [ ] Enable HSTS header
- [ ] Restrict MinIO to internal network
- [ ] Configure firewall rules
- [ ] Set up external SIEM for log aggregation
- [ ] Implement backup encryption
- [ ] Test disaster recovery procedures
- [ ] Document security policies
- [ ] Perform security audit/penetration testing

### Ongoing Security
- [ ] Monitor audit logs daily
- [ ] Review failed login attempts
- [ ] Update dependencies monthly
- [ ] Rotate encryption keys quarterly
- [ ] Test backup restoration monthly
- [ ] Review access logs for anomalies
- [ ] Update security patches immediately
- [ ] Conduct security training for admins

### Monitoring & Alerts
- [ ] Backend service down (critical)
- [ ] High memory usage (warning)
- [ ] High disk usage (critical)
- [ ] Database connection pool exhausted (warning)
- [ ] High error rate (critical)
- [ ] Slow API response time (warning)
- [ ] Failed login attempts (info)
- [ ] Admin actions (info)

---

## Contact & Support

For security issues or vulnerabilities, please contact:
- **Email**: security@udom.ac.tz
- **Response Time**: 24 hours
- **Disclosure**: Responsible disclosure policy

---

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8949)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)

---

**Last Updated**: June 4, 2026
**Version**: 1.0
