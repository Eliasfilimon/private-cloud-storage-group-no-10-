# SYSTEM ARCHITECTURE DIAGRAM
## Secure Self-Hosted Private Cloud Storage System

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER (Frontend)                         │
│                                                                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌─────────────┐ │
│  │    Login     │  │  Dashboard   │  │ File Manager │  │   Profile   │ │
│  │     Page     │  │              │  │              │  │             │ │
│  └──────────────┘  └──────────────┘  └──────────────┘  └─────────────┘ │
│                                                                           │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                     Admin Panel (Admin Only)                      │   │
│  │  • User Management  • Analytics  • Audit Logs  • Reports         │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│                  React.js + Tailwind CSS + Axios                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS/TLS
                                    │
┌─────────────────────────────────────────────────────────────────────────┐
│                       APPLICATION LAYER (Backend)                        │
│                                                                           │
│  ┌────────────────────────────────────────────────────────────────┐     │
│  │                      API GATEWAY / ROUTER                       │     │
│  │              JWT Authentication & Authorization                  │     │
│  └────────────────────────────────────────────────────────────────┘     │
│                                    │                                      │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                        CONTROLLERS LAYER                          │   │
│  │                                                                    │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────┐ │   │
│  │  │    Auth     │ │     File     │ │     User     │ │  Report  │ │   │
│  │  │ Controller  │ │  Controller  │ │  Controller  │ │Controller│ │   │
│  │  └─────────────┘ └──────────────┘ └──────────────┘ └──────────┘ │   │
│  │                                                                    │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────┐              │   │
│  │  │   Backup    │ │    Audit     │ │    Share     │              │   │
│  │  │ Controller  │ │  Controller  │ │  Controller  │              │   │
│  │  └─────────────┘ └──────────────┘ └──────────────┘              │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                    │                                      │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                        SERVICES LAYER                             │   │
│  │                                                                    │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────┐ │   │
│  │  │    User     │ │     File     │ │  Encryption  │ │  Backup  │ │   │
│  │  │   Service   │ │   Service    │ │   Service    │ │  Service │ │   │
│  │  └─────────────┘ └──────────────┘ └──────────────┘ └──────────┘ │   │
│  │                                                                    │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────┐              │   │
│  │  │    Audit    │ │   Storage    │ │    Share     │              │   │
│  │  │   Service   │ │   Service    │ │   Service    │              │   │
│  │  └─────────────┘ └──────────────┘ └──────────────┘              │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                    │                                      │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                       REPOSITORY LAYER                            │   │
│  │                                                                    │   │
│  │  ┌─────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────┐ │   │
│  │  │    User     │ │     File     │ │    Audit     │ │  Backup  │ │   │
│  │  │ Repository  │ │  Repository  │ │  Repository  │ │Repository│ │   │
│  │  └─────────────┘ └──────────────┘ └──────────────┘ └──────────┘ │   │
│  └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│                    Spring Boot + Spring Security                         │
└─────────────────────────────────────────────────────────────────────────┘
                    │                               │
                    │                               │
┌───────────────────▼────────────┐    ┌────────────▼─────────────┐
│     DATA LAYER (Database)      │    │  FILE STORAGE LAYER      │
│                                 │    │                          │
│  ┌──────────────────────────┐  │    │  ┌────────────────────┐ │
│  │    PostgreSQL Database   │  │    │  │  Encrypted Files   │ │
│  │                          │  │    │  │   AES-256-CBC      │ │
│  │  • users                 │  │    │  │                    │ │
│  │  • files (metadata)      │  │    │  │  /uploads/...      │ │
│  │  • file_versions         │  │    │  └────────────────────┘ │
│  │  • audit_logs            │  │    │                          │
│  │  • shared_files          │  │    │  ┌────────────────────┐ │
│  │  • backup_records        │  │    │  │  Backup Storage    │ │
│  │  • sessions              │  │    │  │                    │ │
│  └──────────────────────────┘  │    │  │  /backups/...      │ │
│                                 │    │  └────────────────────┘ │
└─────────────────────────────────┘    └──────────────────────────┘
```

---

## DATA FLOW DIAGRAMS

### 1. USER AUTHENTICATION FLOW
```
┌──────────┐
│  User    │
└────┬─────┘
     │ 1. Enter credentials
     ▼
┌──────────────────┐
│  Login Form      │
└────┬─────────────┘
     │ 2. POST /api/auth/login
     ▼
┌──────────────────────────┐
│  Authentication Service  │
│  • Validate credentials  │
│  • Check BCrypt hash     │
└────┬─────────────────────┘
     │ 3. Query database
     ▼
┌──────────────────┐
│  Users Table     │
└────┬─────────────┘
     │ 4. Return user data
     ▼
┌──────────────────────┐
│  JWT Token Service   │
│  • Generate token    │
│  • 24-hour expiry    │
└────┬─────────────────┘
     │ 5. Return token + user
     ▼
┌──────────────────┐
│  Frontend        │
│  • Store token   │
│  • Store user    │
│  • Redirect      │
└────┬─────────────┘
     │ 6. Access protected routes
     ▼
┌──────────────────┐
│  Dashboard       │
└──────────────────┘
```

### 2. FILE UPLOAD FLOW
```
┌──────────┐
│  User    │
└────┬─────┘
     │ 1. Select file
     ▼
┌────────────────────┐
│  File Upload UI    │
│  • Progress bar    │
└────┬───────────────┘
     │ 2. POST /api/files/upload (multipart)
     │    Headers: Authorization: Bearer <token>
     ▼
┌────────────────────────────┐
│  File Controller           │
│  • Validate JWT            │
│  • Check permissions       │
└────┬───────────────────────┘
     │ 3. Validate file
     ▼
┌────────────────────────────┐
│  Storage Service           │
│  • Check quota             │
│  • Validate file size      │
└────┬───────────────────────┘
     │ 4. Quota OK
     ▼
┌────────────────────────────┐
│  Encryption Service        │
│  • Generate AES key        │
│  • Encrypt file (AES-256)  │
│  • Calculate checksum      │
└────┬───────────────────────┘
     │ 5. Encrypted data
     ▼
┌────────────────────────────┐
│  File System               │
│  • Save to /uploads/       │
│  • Unique filename         │
└────┬───────────────────────┘
     │ 6. File path
     ▼
┌────────────────────────────┐
│  File Metadata Service     │
│  • Create record           │
│  • Store metadata          │
└────┬───────────────────────┘
     │ 7. Save to database
     ▼
┌────────────────────────────┐
│  PostgreSQL                │
│  • files table             │
│  • Update user quota       │
└────┬───────────────────────┘
     │ 8. Success
     ▼
┌────────────────────────────┐
│  Audit Service             │
│  • Log upload action       │
│  • Record IP, timestamp    │
└────┬───────────────────────┘
     │ 9. Return response
     ▼
┌────────────────────────────┐
│  Frontend                  │
│  • Show success message    │
│  • Refresh file list       │
└────────────────────────────┘
```

### 3. FILE DOWNLOAD FLOW
```
┌──────────┐
│  User    │
└────┬─────┘
     │ 1. Click download
     ▼
┌────────────────────────────┐
│  File Manager UI           │
└────┬───────────────────────┘
     │ 2. GET /api/files/{id}/download
     │    Headers: Authorization: Bearer <token>
     ▼
┌────────────────────────────┐
│  File Controller           │
│  • Validate JWT            │
│  • Verify ownership/share  │
└────┬───────────────────────┘
     │ 3. Check permissions
     ▼
┌────────────────────────────┐
│  File Repository           │
│  • Fetch metadata          │
└────┬───────────────────────┘
     │ 4. File metadata
     ▼
┌────────────────────────────┐
│  File System               │
│  • Read encrypted file     │
└────┬───────────────────────┘
     │ 5. Encrypted data
     ▼
┌────────────────────────────┐
│  Encryption Service        │
│  • Decrypt in memory       │
│  • Verify checksum         │
└────┬───────────────────────┘
     │ 6. Decrypted file
     ▼
┌────────────────────────────┐
│  Audit Service             │
│  • Log download action     │
└────┬───────────────────────┘
     │ 7. Stream to browser
     ▼
┌────────────────────────────┐
│  User Browser              │
│  • Download file           │
│  • Original filename       │
└────────────────────────────┘
```

### 4. ADMIN USER CREATION FLOW
```
┌──────────┐
│  Admin   │
└────┬─────┘
     │ 1. Navigate to Admin Panel
     ▼
┌────────────────────────────┐
│  Admin Panel UI            │
│  • Click "Add User"        │
└────┬───────────────────────┘
     │ 2. Fill form & submit
     ▼
┌────────────────────────────┐
│  POST /api/auth/register   │
│  Headers: Authorization    │
└────┬───────────────────────┘
     │ 3. Validate admin role
     ▼
┌────────────────────────────┐
│  Auth Controller           │
│  • Verify admin token      │
│  • Check permissions       │
└────┬───────────────────────┘
     │ 4. Validate input
     ▼
┌────────────────────────────┐
│  User Service              │
│  • Check username unique   │
│  • Check email unique      │
│  • Hash password (BCrypt)  │
└────┬───────────────────────┘
     │ 5. Create user
     ▼
┌────────────────────────────┐
│  User Repository           │
│  • Insert into users table │
└────┬───────────────────────┘
     │ 6. User created
     ▼
┌────────────────────────────┐
│  Audit Service             │
│  • Log user creation       │
│  • Record admin action     │
└────┬───────────────────────┘
     │ 7. Return success
     ▼
┌────────────────────────────┐
│  Admin Panel UI            │
│  • Success notification    │
│  • Close modal             │
│  • Refresh user list       │
└────────────────────────────┘
```

### 5. AUTOMATED BACKUP FLOW
```
┌────────────────────────────┐
│  System Scheduler          │
│  • Cron job (2 AM daily)   │
└────┬───────────────────────┘
     │ 1. Trigger backup
     ▼
┌────────────────────────────┐
│  Backup Service            │
│  • Initialize backup       │
└────┬───────────────────────┘
     │ 2. Dump database
     ▼
┌────────────────────────────┐
│  PostgreSQL Backup         │
│  • pg_dump command         │
│  • Export all tables       │
└────┬───────────────────────┘
     │ 3. Database file
     ▼
┌────────────────────────────┐
│  File System Backup        │
│  • Copy /uploads/ folder   │
│  • Preserve structure      │
└────┬───────────────────────┘
     │ 4. Files copied
     ▼
┌────────────────────────────┐
│  Compression Service       │
│  • Compress backup         │
│  • Calculate checksum      │
└────┬───────────────────────┘
     │ 5. Compressed archive
     ▼
┌────────────────────────────┐
│  Backup Storage            │
│  • Save to /backups/       │
│  • Timestamp filename      │
└────┬───────────────────────┘
     │ 6. Record backup
     ▼
┌────────────────────────────┐
│  Backup Repository         │
│  • Insert backup record    │
│  • Store metadata          │
└────┬───────────────────────┘
     │ 7. Cleanup old backups
     ▼
┌────────────────────────────┐
│  Retention Policy          │
│  • Delete backups > 30 days│
└────┬───────────────────────┘
     │ 8. Log completion
     ▼
┌────────────────────────────┐
│  Audit Log                 │
│  • Record backup success   │
└────────────────────────────┘
```

---

## DATABASE SCHEMA

### Entity Relationship Diagram

```
┌─────────────────────────┐
│        USERS            │
├─────────────────────────┤
│ PK id                   │
│    username (UNIQUE)    │
│    email (UNIQUE)       │
│    password (HASHED)    │
│    full_name            │
│    role (ENUM)          │
│    enabled (BOOLEAN)    │
│    storage_quota        │
│    storage_used         │
│    created_at           │
│    updated_at           │
└──────────┬──────────────┘
           │
           │ 1:N
           │
┌──────────▼──────────────┐
│        FILES            │
├─────────────────────────┤
│ PK id                   │
│    file_name            │
│    file_path (UNIQUE)   │
│    mime_type            │
│    file_size            │
│    checksum             │
│ FK owner_id  ───────────┼──┐
│    parent_folder_id     │  │
│    encrypted (BOOLEAN)  │  │
│    version              │  │
│    uploaded_at          │  │
│    last_accessed_at     │  │
└─────────┬───────────────┘  │
          │                  │
          │ 1:N              │
          │                  │
┌─────────▼───────────────┐  │
│   FILE_VERSIONS         │  │
├─────────────────────────┤  │
│ PK id                   │  │
│ FK file_id              │  │
│    version_number       │  │
│    file_path            │  │
│    file_size            │  │
│    created_at           │  │
│    notes                │  │
└─────────────────────────┘  │
                             │
          ┌──────────────────┘
          │
┌─────────▼───────────────┐
│    AUDIT_LOGS           │
├─────────────────────────┤
│ PK id                   │
│ FK user_id              │
│ FK file_id (NULLABLE)   │
│    action (ENUM)        │
│    ip_address           │
│    details              │
│    timestamp            │
└─────────────────────────┘

┌─────────────────────────┐
│    SHARED_FILES         │
├─────────────────────────┤
│ PK id                   │
│ FK file_id              │
│ FK shared_by (user_id)  │
│ FK shared_with (user_id)│
│    permissions (ENUM)   │
│    expires_at           │
│    created_at           │
└─────────────────────────┘

┌─────────────────────────┐
│   BACKUP_RECORDS        │
├─────────────────────────┤
│ PK id                   │
│    backup_path          │
│    backup_size          │
│    checksum             │
│    backup_type (ENUM)   │
│    status (ENUM)        │
│    created_at           │
│    completed_at         │
└─────────────────────────┘
```

---

## SECURITY ARCHITECTURE

```
┌────────────────────────────────────────────────────┐
│              SECURITY LAYERS                        │
├────────────────────────────────────────────────────┤
│                                                     │
│  Layer 1: Network Security                         │
│  ┌──────────────────────────────────────────────┐ │
│  │  • HTTPS/TLS Encryption                      │ │
│  │  • Certificate Validation                    │ │
│  │  • Firewall Rules                            │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Layer 2: Authentication                           │
│  ┌──────────────────────────────────────────────┐ │
│  │  • JWT Token-Based Auth                      │ │
│  │  • BCrypt Password Hashing                   │ │
│  │  • Session Management                        │ │
│  │  • Token Expiration (24h)                    │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Layer 3: Authorization                            │
│  ┌──────────────────────────────────────────────┐ │
│  │  • Role-Based Access Control (RBAC)          │ │
│  │  • Resource Ownership Check                  │ │
│  │  • Permission Verification                   │ │
│  │  • API Endpoint Protection                   │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Layer 4: Data Encryption                          │
│  ┌──────────────────────────────────────────────┐ │
│  │  • AES-256-CBC File Encryption               │ │
│  │  • Encrypted File Storage                    │ │
│  │  • Secure Key Management                     │ │
│  │  • Checksum Verification                     │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Layer 5: Audit & Monitoring                       │
│  ┌──────────────────────────────────────────────┐ │
│  │  • Comprehensive Activity Logging            │ │
│  │  • Security Event Tracking                   │ │
│  │  • Failed Login Monitoring                   │ │
│  │  • Access Pattern Analysis                   │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
│  Layer 6: Input Validation                         │
│  ┌──────────────────────────────────────────────┐ │
│  │  • SQL Injection Prevention                  │ │
│  │  • XSS Protection                            │ │
│  │  • CSRF Protection                           │ │
│  │  • File Type Validation                      │ │
│  └──────────────────────────────────────────────┘ │
│                                                     │
└────────────────────────────────────────────────────┘
```

---

**Document**: System Architecture  
**Version**: 1.0  
**Date**: February 16, 2026  
**Team**: Group 10 - UDOM CSE
