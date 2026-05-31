# Copilot Instructions for Secure Cloud Storage

## Project Overview
A self-hosted private cloud storage system for university staff (admin & lecturer roles). Full-stack Java/React app with encrypted file storage (AES-256-CBC), JWT authentication, audit logging, and file sharing capabilities.

**Key constraint:** Staff-only system—no public registration. Admins create users via `/auth/register` endpoint, requiring a bootstrapping solution for the first admin user via `/auth/signup`.

## Architecture Pattern

### Client-Server with JWT Auth
- **Frontend** (React + Vite): http://localhost:5173
- **Backend** (Spring Boot): http://localhost:8080/api
- **Database** (PostgreSQL): Managed via Spring Data JPA with auto-schema migration (`ddl-auto=update`)

**Auth flow:** Client stores JWT in localStorage → Axios interceptor auto-adds `Authorization: Bearer <token>` → Backend validates with JwtProvider → Unauthorized (401) clears token and redirects to login.

## Critical Developer Workflows

### Setup: First Time
1. Start PostgreSQL (default: localhost:5432, user: postgres, password: postgres)
2. Backend: `cd backend && ./mvnw spring-boot:run`
3. Frontend: `cd frontend && npm install && npm run dev`
4. **Bootstrap first admin:** POST to `http://localhost:8080/api/auth/signup` with role=ADMIN (public endpoint, fails after first user)
5. Login via UI with admin credentials

### Build & Run
- **Backend:** `./mvnw spring-boot:run` or `./mvnw clean package && java -jar target/secure-cloud-storage-1.0.0.jar`
- **Frontend:** `npm run dev` (dev) or `npm run build && npm run preview` (production preview)
- **Run scripts:** `backend/run.sh` (full setup + database prompt), `backend/setup-database.sh` (schema initialization)

### Database
- Schema auto-migrates on startup (`spring.jpa.hibernate.ddl-auto=update`)
- Connection pool: HikariCP (max 10 connections, min 5)
- Location: `backend/src/main/resources/database/schema.sql` (reference schema)
- Logs & backups: `backend/logs/` and `backend/backups/`

## Code Organization & Patterns

### Backend Layer Structure (Spring Boot 3.2, Java 17)
- **Controller** (`controller/`): REST endpoints, input validation via `@Valid` + DTO annotations
- **Service** (`service/`): Business logic—file encryption/decryption, audit logging, user/file operations
- **Repository** (`repository/`): Spring Data JPA interfaces extending `CrudRepository` or `JpaRepository`
- **Model** (`model/`): JPA entities with `@Entity`, `@Table`, `@Transactional` service methods for multi-step ops
- **DTO** (`dto/`): Request/response objects with JSR-303 validators (`@NotBlank`, `@Email`, `@Size`)
- **Security** (`security/`): `JwtProvider` (token creation/validation), `JwtAuthFilter` (request interception), `SecurityConfig` (bean definitions)
- **Config** (`config/`): `CorsConfig` (allows localhost:3000), file storage paths, encryption keys
- **Exception** (`exception/`): Custom `AppException` (generic), caught by global `@ControllerAdvice` handler

**Key implementation detail:** File encryption service uses AES-256-CBC with random IV per file; encryption keys must be secure in prod (move from application.properties to KeyVault).

### Frontend Component Structure (React 18, Vite)
- **Pages** (`pages/`): Full-page components—Login, FileManager, AdminPanel, Dashboard
- **Components** (`components/`): Reusable—Navbar, PrivateRoute (checks localStorage token), FileUploadModal
- **Services** (`services/api.js`): Axios instance with interceptors (JWT injection, 401 handling)
- **Routing** (React Router v6): PrivateRoute wrapper validates token before rendering protected pages

**Key pattern:** Use `localStorage` for token persistence; PrivateRoute checks `localStorage.getItem('token')` and redirects to /login if missing.

## API Conventions

### Endpoint Organization
- **Auth**: `/api/auth/login`, `/api/auth/register` (admin-only), `/api/auth/signup` (public, first-user only), `/api/auth/me`
- **Files**: `/api/files/upload`, `/api/files`, `/api/files/{id}`, `/api/files/{id}/download`, `/api/files/{id}/delete`
- **Users** (admin): `/api/users`, `/api/users/{id}`, `/api/users/{id}/toggle-status`
- **Sharing** (if implemented): Check ShareFileModal.jsx and FileStorageService for patterns

### Error Handling
- All errors wrapped in JSON: `{ success: false, message: "error description", code: "ERROR_CODE" }`
- HTTP status codes: 200 (success), 400 (validation), 401 (auth), 403 (forbidden), 404 (not found), 500 (server)
- Frontend: `api.js` response interceptor catches 401, clears auth, redirects to login; components use try-catch with toast notifications

### Data Format
- **File upload:** multipart/form-data (use FormData)
- **JSON payloads:** `Content-Type: application/json`
- **File metadata:** name, size, mimetype, owner, uploadDate, encrypted flag

## Security Specifics

1. **Authentication:** JWT (HS256 algorithm, secret in application.properties—change in production)
2. **Password:** BCrypt encoding (Spring Security auto-handles via `passwordEncoder()` bean)
3. **Encryption:** AES-256-CBC for file contents (not metadata); keys in application.properties
4. **CORS:** Limited to `http://localhost:3000` (frontend); update for production
5. **File storage:** Disk-based (`file.upload-dir=./uploads`); organize by user directory for isolation
6. **Audit logging:** AuditLogService captures user actions with IP address, timestamp, user agent

## Common Coding Patterns

### Adding an API Endpoint
1. Create DTO in `dto/RequestNameDTO.java` with validators
2. Add method to service (`service/ServiceName.java`)
3. Create endpoint in controller with `@PostMapping`, `@GetMapping`, etc.; inject service
4. Frontend: Add API call in `api.js` (authAPI, filesAPI objects pattern)
5. Component: Call API in useEffect or event handler, handle error with toast

### File Operations
- Upload: `FileStorageService.saveFile(file, userId)` → returns encrypted file path
- Download: `FileStorageService.getFile(fileId)` → returns decrypted bytes
- Delete: Check ownership, call repository delete, remove physical file
- Encrypt/decrypt: Use `FileEncryptionService` (AES-256-CBC)

### Admin-Only Operations
- Inject `JwtProvider` to validate token and check role (`extractRole()`)
- Return 403 Forbidden if not ADMIN role
- Example: `/api/users` GET endpoint checks `role == ADMIN`

## Environment & Configuration

### Key Properties (application.properties)
- `spring.datasource.url`: PostgreSQL connection string
- `jwt.secret`: Base64-encoded secret for token signing (production: use environment variable)
- `jwt.expiration`: Token TTL in ms (default: 86400000 = 24h)
- `file.upload-dir`: Disk path for uploaded files (default: `./uploads`)
- `cors.allowed-origins`: Frontend URL (default: `http://localhost:3000`)
- `spring.jpa.hibernate.ddl-auto`: Schema update strategy (set to `validate` in production)

### Build Artifacts
- Backend: `target/secure-cloud-storage-1.0.0.jar`
- Frontend: `dist/` directory after `npm run build`

## Testing & Debugging

- **Backend logs:** `logs/application.log` (DEBUG level for `tz.ac.udom.*`, Spring Security)
- **Frontend dev tools:** Browser console → Network tab to inspect API calls
- **Database inspection:** Use `psql` CLI or any PostgreSQL GUI tool
- **Common issue:** Ensure both backend and frontend are running; check CORS settings if frontend can't reach API

## File Sharing & Advanced Features

Check [FILE-SHARING-IMPLEMENTATION.md](../FILE-SHARING-IMPLEMENTATION.md) for implementation details on:
- ShareFileModal.jsx pattern for granular permission selection
- Shared files API endpoint
- Permission model (view, download, upload)

## Key Files to Understand First
1. [backend/src/main/resources/application.properties](../backend/src/main/resources/application.properties) - Configuration
2. [backend/src/main/java/com/udom/securecloud/security/JwtProvider.java](../backend/src/main/java/com/udom/securecloud/security/JwtProvider.java) - Token handling
3. [frontend/src/services/api.js](../frontend/src/services/api.js) - API client & interceptors
4. [backend/src/main/java/com/udom/securecloud/service/FileStorageService.java](../backend/src/main/java/com/udom/securecloud/service/FileStorageService.java) - File encryption/decryption
5. [ARCHITECTURE.md](../ARCHITECTURE.md) - Visual system diagram
