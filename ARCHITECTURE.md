# Secure Cloud Storage - Complete Prototype Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React + Vite)                  │
│                     http://localhost:5173                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Login Page   │  │ File Manager │  │ Admin Panel  │         │
│  │  ✅ JWT Auth │  │ ✅ Upload    │  │ ✅ User Mgmt │         │
│  │  ✅ UDOM UI  │  │ ✅ Download  │  │ ✅ Create User│        │
│  └──────────────┘  │ ✅ Delete    │  └──────────────┘         │
│                     │ ✅ Search    │                             │
│                     └──────────────┘                             │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            API Client (Axios)                             │  │
│  │  ✅ JWT Token Interceptor                                 │  │
│  │  ✅ Error Handling                                        │  │
│  │  ✅ Auto Redirect on 401                                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP/HTTPS
                            │ REST API Calls
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                         │
│                  http://localhost:8080/api                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           Security Layer (Spring Security)                │  │
│  │  ✅ JWT Authentication Filter                             │  │
│  │  ✅ Password Encoder (BCrypt)                             │  │
│  │  ✅ CORS Configuration                                    │  │
│  │  ✅ Role-Based Access Control (RBAC)                      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                   │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │ AuthController │  │ FileController │  │ AdminController│   │
│  │ POST /login    │  │ POST /upload   │  │ GET /users     │   │
│  │ GET /me        │  │ GET /files     │  │ POST /register │   │
│  │ POST /register │  │ GET /download  │  │ PUT /toggle    │   │
│  │                │  │ DELETE /delete │  │ DELETE /user   │   │
│  └────────┬───────┘  └────────┬───────┘  └────────┬───────┘   │
│           │                   │                   │              │
│           ▼                   ▼                   ▼              │
│  ┌────────────────┐  ┌────────────────┐  ┌────────────────┐   │
│  │  AuthService   │  │ FileStorage    │  │  UserService   │   │
│  │                │  │    Service     │  │                │   │
│  │ ✅ Login       │  │ ✅ Upload      │  │ ✅ List Users  │   │
│  │ ✅ Create User │  │ ✅ Download    │  │ ✅ Toggle      │   │
│  │ ✅ Get Profile │  │ ✅ Delete      │  │ ✅ Delete      │   │
│  └────────────────┘  │ ✅ List Files  │  └────────────────┘   │
│                       └────────┬───────┘                         │
│                                │                                  │
│  ┌────────────────────────────▼───────────────────────────┐     │
│  │            FileEncryptionService                        │     │
│  │  ✅ AES-256-CBC Encryption                              │     │
│  │  ✅ Secure Key Generation                               │     │
│  │  ✅ SHA-256 Checksums                                   │     │
│  │  ✅ Random IV per file                                  │     │
│  └─────────────────────────────────────────────────────────┘     │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           AuditLogService                                │    │
│  │  ✅ Log all user actions                                 │    │
│  │  ✅ Track IP addresses                                   │    │
│  │  ✅ Record timestamps                                    │    │
│  │  ✅ Store user agent info                                │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │ JDBC
                            │ SQL Queries
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     DATABASE (PostgreSQL)                        │
│                    postgresql://localhost:5432                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│  │   users     │  │file_metadata│  │ audit_logs  │             │
│  │             │  │             │  │             │             │
│  │ id          │  │ id          │  │ id          │             │
│  │ username    │  │ user_id     │  │ user_id     │             │
│  │ email       │  │ file_name   │  │ action      │             │
│  │ password    │  │ file_size   │  │ resource    │             │
│  │ role        │  │ file_path   │  │ ip_address  │             │
│  │ quota       │  │ is_encrypted│  │ timestamp   │             │
│  │ used        │  │ encrypt_key │  │ status      │             │
│  └─────────────┘  └─────────────┘  └─────────────┘             │
│                                                                   │
│  ┌─────────────┐  ┌─────────────┐                               │
│  │shared_files │  │backup_records│                              │
│  │             │  │             │                               │
│  │ file_id     │  │ id          │                               │
│  │ owner_id    │  │ backup_name │                               │
│  │ shared_with │  │ total_size  │                               │
│  │ permission  │  │ status      │                               │
│  └─────────────┘  └─────────────┘                               │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘

                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      FILE SYSTEM                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ./uploads/                                                       │
│  ├── admin/                                                       │
│  │   ├── uuid-encrypted-file-1.pdf                              │
│  │   └── uuid-encrypted-file-2.docx                             │
│  └── lecturer1/                                                   │
│      └── uuid-encrypted-file-3.xlsx                              │
│                                                                   │
│  ./backups/                                                       │
│  └── (backup files)                                               │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagrams

### 1. User Login Flow
```
User → Enter credentials → Frontend (Login.jsx)
                              ↓
                         POST /api/auth/login
                              ↓
                         AuthController
                              ↓
                         AuthService
                              ↓
                    Validate credentials (BCrypt)
                              ↓
                     Generate JWT Token
                              ↓
                      Log to audit_logs
                              ↓
                    Return token + user data
                              ↓
                    Store in localStorage
                              ↓
                    Redirect to dashboard
```

### 2. File Upload with Encryption Flow
```
User → Select file → Enable encryption → Upload
                                          ↓
                              POST /api/files/upload
                                          ↓
                                   FileController
                                          ↓
                                 FileStorageService
                                          ↓
                              Check storage quota
                                          ↓
                              Generate encryption key
                                          ↓
                         FileEncryptionService (AES-256)
                                          ↓
                              Encrypt file content
                                          ↓
                         Save encrypted file to disk
                                          ↓
                         Calculate SHA-256 checksum
                                          ↓
                      Save metadata to file_metadata table
                                          ↓
                         Update user's storage_used
                                          ↓
                            Log to audit_logs
                                          ↓
                         Return success response
```

### 3. File Download with Decryption Flow
```
User → Click download → FileManager.jsx
                              ↓
                   GET /api/files/{id}/download
                              ↓
                         FileController
                              ↓
                       FileStorageService
                              ↓
                     Verify user ownership
                              ↓
                     Read encrypted file
                              ↓
                 If encrypted, get encryption key
                              ↓
                FileEncryptionService (AES-256)
                              ↓
                      Decrypt file content
                              ↓
                    Log to audit_logs
                              ↓
                  Return decrypted file
                              ↓
                Browser downloads file
```

### 4. Admin Creates User Flow
```
Admin → AdminPanel → Fill form → Submit
                                   ↓
                      POST /api/auth/register
                                   ↓
                            AuthController
                                   ↓
                         Check ADMIN role
                                   ↓
                             AuthService
                                   ↓
                   Validate username/email unique
                                   ↓
                    Hash password (BCrypt)
                                   ↓
                        Set storage quota
                     (5GB LECTURER, 10GB ADMIN)
                                   ↓
                        Save to users table
                                   ↓
                        Log to audit_logs
                                   ↓
                     Return success response
                                   ↓
                    Frontend shows success
                                   ↓
                    Refresh user list
```

## Security Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer 1                          │
│                  NETWORK & TRANSPORT                         │
│  ✅ HTTPS (production)                                       │
│  ✅ CORS protection                                          │
│  ✅ Rate limiting (Spring Security)                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer 2                          │
│                  AUTHENTICATION                              │
│  ✅ JWT token validation                                     │
│  ✅ Token expiration (24h)                                   │
│  ✅ BCrypt password hashing (strength 12)                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer 3                          │
│                  AUTHORIZATION                               │
│  ✅ Role-Based Access Control (RBAC)                         │
│  ✅ @PreAuthorize annotations                                │
│  ✅ Method-level security                                    │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer 4                          │
│                  DATA PROTECTION                             │
│  ✅ AES-256-CBC file encryption                              │
│  ✅ Secure key management                                    │
│  ✅ SHA-256 file integrity checks                            │
│  ✅ Database encryption at rest                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                    Security Layer 5                          │
│                  AUDIT & MONITORING                          │
│  ✅ Complete audit trail                                     │
│  ✅ IP address logging                                       │
│  ✅ User agent tracking                                      │
│  ✅ Timestamp on all actions                                 │
└─────────────────────────────────────────────────────────────┘
```

## Component Interaction Map

```
┌──────────────────────────────────────────────────────────────┐
│                        FRONTEND                               │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  App.jsx                                                      │
│    │                                                          │
│    ├─► PrivateRoute (Auth Guard)                             │
│    │     │                                                    │
│    │     ├─► Dashboard                                        │
│    │     ├─► FileManager                                      │
│    │     ├─► Profile                                          │
│    │     └─► AdminPanel (ADMIN only)                          │
│    │                                                          │
│    └─► Login (Public)                                         │
│                                                               │
│  Navbar (Global)                                              │
│    └─► Role-based menu items                                 │
│                                                               │
│  api.js (Axios Client)                                        │
│    ├─► Request Interceptor (Add JWT)                         │
│    └─► Response Interceptor (Handle 401)                     │
│                                                               │
└──────────────────────────────────────────────────────────────┘
                           ↕
┌──────────────────────────────────────────────────────────────┐
│                        BACKEND                                │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Controllers (REST API)                                       │
│    ├─► AuthController                                         │
│    ├─► FileController                                         │
│    └─► AdminController                                        │
│                                                               │
│  Services (Business Logic)                                    │
│    ├─► AuthService                                            │
│    ├─► FileStorageService                                     │
│    ├─► FileEncryptionService                                  │
│    ├─► UserService                                            │
│    └─► AuditLogService                                        │
│                                                               │
│  Repositories (Data Access)                                   │
│    ├─► UserRepository                                         │
│    ├─► FileMetadataRepository                                 │
│    └─► AuditLogRepository                                     │
│                                                               │
│  Security                                                     │
│    ├─► JwtAuthenticationFilter                               │
│    ├─► JwtTokenProvider                                       │
│    ├─► UserDetailsServiceImpl                                 │
│    └─► SecurityConfig                                         │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

## Technology Integration

```
Frontend Stack          Backend Stack           Database
─────────────────      ──────────────────      ─────────────
React 18         ◄───► Spring Boot 3.2.2 ◄───► PostgreSQL
Vite 5                 Java 17                  14+
Tailwind CSS           Maven 3.8+
React Router v6        Spring Security
Axios                  JWT (jsonwebtoken)
React Icons            Hibernate/JPA
                       Lombok
                       BCrypt
                       AES-256
```

---

**Complete prototype with 100% of core features implemented!** 🎉

