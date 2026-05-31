<div align="center">

# ⚙️ UDOM Secure Cloud Storage — Backend

### Enterprise-Grade REST API with AES-256 Encryption

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MinIO](https://img.shields.io/badge/MinIO-Storage-C72E49?style=for-the-badge&logo=minio&logoColor=white)](https://min.io/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)

</div>

---

A robust Spring Boot REST API providing secure file storage with **AES-256-GCM encryption**, JWT authentication, role-based access control, and comprehensive audit logging for the UDOM Secure Cloud Storage system.

---

## Table of Contents

- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Docker](#docker)
- [Troubleshooting](#troubleshooting)

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 3.2.2 |
| Language | Java | 17+ |
| Database | PostgreSQL | 15+ |
| ORM | Spring Data JPA | - |
| Security | Spring Security + JWT | - |
| Storage | MinIO (S3-compatible) | Latest |
| Build Tool | Maven | 3.8+ |

---

## Project Structure

```
backend/
├── src/main/java/com/udom/securecloud/
│   ├── SecureCloudStorageApplication.java    # Entry point
│   ├── config/                               # Configuration classes
│   │   ├── SecurityConfig.java               # Security & CORS
│   │   ├── MinioConfig.java                  # MinIO storage
│   │   └── WebConfig.java                    # Web configuration
│   ├── controller/                           # REST controllers
│   │   ├── AuthController.java               # Authentication
│   │   ├── FileController.java               # File operations
│   │   ├── FolderController.java             # Folder management
│   │   └── AdminController.java              # Admin operations
│   ├── service/                              # Business logic
│   │   ├── AuthService.java
│   │   ├── FileService.java
│   │   ├── FolderService.java
│   │   ├── UserService.java
│   │   └── AuditLogService.java
│   ├── repository/                           # Data access layer
│   │   ├── UserRepository.java
│   │   ├── FileMetadataRepository.java
│   │   ├── FolderRepository.java
│   │   └── AuditLogRepository.java
│   ├── model/                                  # Entity classes
│   │   ├── User.java
│   │   ├── FileMetadata.java
│   │   ├── Folder.java
│   │   └── AuditLog.java
│   ├── dto/                                    # Data transfer objects
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   ├── FileUploadRequest.java
│   │   └── UserDTO.java
│   ├── security/                               # Security components
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── BruteForceProtectionService.java
│   ├── exception/                              # Custom exceptions
│   └── util/                                   # Utility classes
│       ├── FileEncryptionUtil.java
│       └── PasswordValidator.java
├── src/main/resources/
│   ├── application.properties                  # Main config
│   ├── application-prod.properties             # Production config
│   └── database/
│       └── schema.sql                          # Database schema
├── src/test/                                   # Unit & integration tests
├── Dockerfile                                  # Container image
├── pom.xml                                     # Maven configuration
└── mvnw, mvnw.cmd                              # Maven wrapper
```

---

## Quick Start

### Option 1: Docker (Recommended for Deployment)

```bash
# From project root
docker compose up -d backend
```

### Option 2: Local Development

#### Prerequisites
- Java JDK 17+
- Maven 3.8+ (or use included wrapper)
- PostgreSQL 15+
- MinIO (optional, for file storage)

#### Step 1: Database Setup

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE secure_cloud_storage;

# Exit
\q
```

#### Step 2: Configure Application

Create/edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/secure_cloud_storage
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT
jwt.secret=your-super-secret-jwt-key-change-in-production
jwt.expiration=86400000

# File Storage (Local or MinIO)
file.upload.dir=./uploads
file.backup.location=./backups

# MinIO (optional)
minio.url=http://localhost:9002
minio.access-key=minioadmin
minio.secret-key=minioadmin123
minio.bucket-name=secure-cloud-storage
```

#### Step 3: Run the Application

**Linux/macOS:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

**Or with Maven installed:**
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

---

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/secure_cloud_storage` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` | Yes |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Hibernate DDL mode | `update` | No |
| `JWT_SECRET` | JWT signing secret | Auto-generated | **Yes** |
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` | No |
| `FILE_UPLOAD_DIR` | Local upload directory | `/app/uploads` | No |
| `FILE_BACKUP_LOCATION` | Backup directory | `/app/backups` | No |
| `MINIO_URL` | MinIO server URL | `http://localhost:9002` | No |
| `MINIO_ACCESS_KEY` | MinIO access key | `minioadmin` | No |
| `MINIO_SECRET_KEY` | MinIO secret key | `minioadmin123` | **Yes** |
| `MINIO_BUCKET_NAME` | MinIO bucket name | `secure-cloud-storage` | No |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3002` | No |
| `SERVER_PORT` | Application port | `8080` | No |

### Profile-Specific Configuration

- **Development**: `application.properties` (default)
- **Production**: `application-prod.properties`
- **Docker**: Environment variables override properties

---

## API Documentation

### Base URL
- Local: `http://localhost:8080`
- Docker: `http://localhost:8080`
- API Prefix: `/api`

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/register` | Register new user (Admin only) | Yes (ADMIN) |
| POST | `/api/auth/signup` | Alternative registration | No |
| GET | `/api/auth/me` | Get current user profile | Yes |

**Login Request:**
```json
{
  "username": "admin@udom.ac.tz",
  "password": "Admin@123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "username": "admin@udom.ac.tz",
    "email": "admin@udom.ac.tz",
    "fullName": "System Administrator",
    "role": "ADMIN"
  }
}
```

### File Management Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/files` | List user's files | Yes |
| GET | `/api/files/{id}` | Get file metadata | Yes |
| POST | `/api/files/upload` | Upload new file | Yes |
| GET | `/api/files/download/{id}` | Download file | Yes |
| DELETE | `/api/files/{id}` | Delete file (soft delete) | Yes |
| PUT | `/api/files/{id}` | Update file metadata | Yes |
| GET | `/api/files/trash` | List trashed files | Yes |
| POST | `/api/files/restore/{id}` | Restore from trash | Yes |
| DELETE | `/api/files/permanent/{id}` | Permanent delete | Yes (Owner/Admin) |

**Upload Request:**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@document.pdf" \
  -F "folderId=1" \
  -F "description=Important document"
```

### Folder Management Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/folders` | List all folders | Yes |
| GET | `/api/folders/{id}` | Get folder details | Yes |
| POST | `/api/folders` | Create new folder | Yes |
| PUT | `/api/folders/{id}` | Update folder | Yes |
| DELETE | `/api/folders/{id}` | Delete folder | Yes |
| GET | `/api/folders/{id}/files` | List files in folder | Yes |
| GET | `/api/folders/root` | Get root folder contents | Yes |

**Create Folder Request:**
```json
{
  "folderName": "Documents",
  "parentFolderId": null,
  "folderColor": "#4A90E2"
}
```

### Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/users` | List all users | Yes (ADMIN) |
| GET | `/api/admin/users/{id}` | Get user details | Yes (ADMIN) |
| POST | `/api/admin/users` | Create new user | Yes (ADMIN) |
| PUT | `/api/admin/users/{id}` | Update user | Yes (ADMIN) |
| DELETE | `/api/admin/users/{id}` | Deactivate user | Yes (ADMIN) |
| GET | `/api/admin/audit-logs` | View audit logs | Yes (ADMIN) |
| GET | `/api/admin/dashboard-stats` | System statistics | Yes (ADMIN) |
| GET | `/api/admin/storage-report` | Storage usage report | Yes (ADMIN) |

**Create User Request:**
```json
{
  "username": "newuser@udom.ac.tz",
  "email": "newuser@udom.ac.tz",
  "password": "SecurePass123!",
  "fullName": "New User",
  "role": "STAFF",
  "department": "Computer Science",
  "storageQuota": 5368709120
}
```

### Share Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/shares` | Share file with user | Yes |
| GET | `/api/shares/shared-with-me` | Files shared with me | Yes |
| GET | `/api/shares/shared-by-me` | Files I shared | Yes |
| DELETE | `/api/shares/{id}` | Revoke share | Yes |

### Actuator/Health Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health status |
| GET | `/actuator/info` | Application info |
| GET | `/actuator/metrics` | Application metrics |

---

## Security

### Authentication Flow

1. User submits credentials to `/api/auth/login`
2. Server validates credentials and generates JWT token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token on each protected request
5. Token expires after configured duration (default: 24 hours)

### Security Features

| Feature | Implementation |
|---------|----------------|
| Authentication | JWT (JSON Web Tokens) |
| Password Hashing | BCrypt (strength 12) |
| File Encryption | AES-256 at rest |
| Brute Force Protection | Account lockout after 5 failed attempts |
| CORS | Configured for specific origins |
| SQL Injection Prevention | Parameterized queries (JPA) |
| XSS Protection | Input validation & output encoding |
| Role-Based Access | Spring Security annotations |

### Brute Force Protection

- **Max Attempts**: 5 failed login attempts
- **Lockout Duration**: 15 minutes
- **Tracking**: By username identifier

---

## Database Schema

### Core Tables

**users** - System users
```sql
- id (BIGSERIAL PRIMARY KEY)
- username (VARCHAR(50) UNIQUE)
- email (VARCHAR(100) UNIQUE)
- password (VARCHAR(255)) -- BCrypt hashed
- full_name (VARCHAR(100))
- role (VARCHAR(20)) -- ADMIN or STAFF
- department (VARCHAR(100))
- storage_quota (BIGINT) -- Bytes
- storage_used (BIGINT) -- Bytes
- is_active (BOOLEAN)
- totp_enabled (BOOLEAN)
- created_at, updated_at, last_login (TIMESTAMP)
```

**file_metadata** - File information
```sql
- id (BIGSERIAL PRIMARY KEY)
- user_id (FK to users)
- folder_id (FK to folders, nullable)
- file_name, original_name, file_path
- file_size, mime_type
- is_encrypted (BOOLEAN)
- encryption_key_id (VARCHAR)
- is_deleted, deleted_at (soft delete)
- created_at, updated_at
```

**folders** - Folder structure
```sql
- id (BIGSERIAL PRIMARY KEY)
- user_id (FK to users)
- parent_folder_id (self-referencing FK, nullable)
- folder_name, folder_color
- is_deleted (BOOLEAN)
- created_at, updated_at
```

**audit_logs** - System audit trail
```sql
- id (BIGSERIAL PRIMARY KEY)
- user_id (FK to users, nullable)
- action (VARCHAR) -- LOGIN, LOGOUT, UPLOAD, DELETE, etc.
- entity_type, entity_id
- old_value, new_value (JSONB)
- ip_address, user_agent
- timestamp
```

For complete schema, see `src/main/resources/database/schema.sql`

---

## Testing

### Run Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Run with coverage report
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dtest=*IntegrationTest
```

### Test Structure

```
src/test/java/com/udom/securecloud/
├── unit/                           # Unit tests
│   ├── service/
│   ├── controller/
│   └── security/
├── integration/                    # Integration tests
│   ├── repository/
│   └── api/
└── resources/                      # Test resources
```

---

## Docker

### Build Image

```bash
# From project root
docker build -t secure-cloud-storage-backend ./backend
```

### Run Container

```bash
docker run -d \
  --name scs_backend \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:55432/secure_cloud_storage \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=your-secret-key \
  -e MINIO_URL=http://host.docker.internal:9002 \
  secure-cloud-storage-backend
```

### Docker Compose (Recommended)

See root `docker-compose.yml` for orchestrated setup with all services.

---

## Troubleshooting

### Common Issues

#### Application Won't Start

```bash
# Check if port 8080 is in use
lsof -i :8080

# Check database connection
./mvnw spring-boot:run -Ddebug

# Verify Java version
java -version  # Should be 17+
```

#### Database Connection Failed

```bash
# Verify PostgreSQL is running
pg_isready -h localhost -p 5432

# Check database exists
psql -U postgres -c "\l" | grep secure_cloud_storage

# Check credentials in application.properties
cat src/main/resources/application.properties | grep spring.datasource
```

#### JWT Token Issues

```bash
# Generate a secure JWT secret
openssl rand -base64 64

# Set in application.properties or environment
export JWT_SECRET=your-generated-secret
```

#### Build Failures

```bash
# Clean and rebuild
./mvnw clean install

# Skip tests if needed (not recommended for CI)
./mvnw clean install -DskipTests

# Update dependencies
./mvnw dependency:resolve
```

### Logs

```bash
# Application logs location
./logs/application.log

# Docker logs
docker logs scs_backend

# Enable debug logging
# Add to application.properties:
logging.level.com.udom.securecloud=DEBUG
```

### Performance Tuning

```properties
# Connection pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA batch operations
spring.jpa.properties.hibernate.jdbc.batch_size=20

# File upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

---

## Production Deployment

### Production Checklist

Before deploying to production:

- [ ] Use `validate` for `SPRING_JPA_HIBERNATE_DDL_AUTO` (not `update`)
- [ ] Configure connection pooling (HikariCP)
- [ ] Enable G1GC garbage collector
- [ ] Set up log aggregation (ELK/Loki)
- [ ] Configure health check endpoints
- [ ] Enable JMX monitoring
- [ ] Set up distributed tracing (optional)

### Production Configuration

`application-prod.properties`:

```properties
# Database - Production Pool Settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000

# JPA - Validate only (no schema changes)
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Logging
logging.level.root=WARN
logging.level.com.udom.securecloud=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.file.name=/app/logs/application.log
logging.logback.rollingpolicy.max-file-size=100MB
logging.logback.rollingpolicy.max-history=30

# Actuator - Security
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized
management.metrics.tags.application=${spring.application.name}

# Security
jwt.expiration=43200000  # 12 hours in production
```

### JVM Tuning for Production

```bash
# Recommended JVM options
JAVA_OPTS="-server \
  -Xms2g \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+AlwaysPreTouch \
  -XX:+DisableExplicitGC \
  -XX:+ParallelRefProcEnabled \
  -XX:+UseStringDeduplication \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.backgroundpreinitializer.ignore=true"
```

### Docker Production Build

```bash
# Build production image
docker build -t secure-cloud-storage-backend:prod .

# Run with production settings
docker run -d \
  --name scs_backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JAVA_OPTS="-Xmx2g -Xms2g -XX:+UseG1GC" \
  -e JWT_SECRET=<production-secret> \
  -v /host/logs:/app/logs \
  --health-cmd="wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1" \
  --health-interval=30s \
  secure-cloud-storage-backend:prod
```

## Monitoring & Observability

### Health Endpoints

| Endpoint | Description | Auth Required |
|----------|-------------|---------------|
| `/actuator/health` | Overall health status | No |
| `/actuator/health/db` | Database connectivity | No |
| `/actuator/health/diskSpace` | Disk space status | No |
| `/actuator/metrics` | All metrics | No |
| `/actuator/metrics/jvm.memory.used` | JVM memory | No |
| `/actuator/prometheus` | Prometheus format | No |

### Key Metrics to Monitor

```
# Application Metrics
http_server_requests_seconds_count{status="5xx"}  # Error rate
http_server_requests_seconds_sum                  # Response time
jvm_memory_used_bytes                            # Memory usage
jvm_threads_live_threads                         # Thread count
process_cpu_usage                                # CPU usage

# Database Metrics
hikaricp_connections_active                      # Active connections
hikaricp_connections_pending                     # Waiting connections

# Custom Metrics
scs.files.uploaded.total                         # Total uploads
scs.files.downloaded.total                       # Total downloads
scs.storage.used.bytes                           # Storage usage
scs.users.active.count                           # Active users
```

### Log Format (Production)

```
2024-01-15 14:32:15 [INFO] [AuthService] User admin@udom.ac.tz logged in successfully [ip: 192.168.1.100]
2024-01-15 14:32:45 [INFO] [FileService] File uploaded: document.pdf [user: admin@udom.ac.tz, size: 2.5MB]
2024-01-15 14:33:10 [WARN] [BruteForceProtection] Multiple failed attempts detected [user: unknown, ip: 10.0.0.50]
```

## Security Hardening

### Production Security Checklist

- [ ] **JWT Secret**: Minimum 256-bit cryptographically secure random key
- [ ] **Password Policy**: Enforce strong passwords (min 12 chars, complexity)
- [ ] **HTTPS Only**: Configure HSTS headers
- [ ] **Rate Limiting**: Implement request throttling
- [ ] **CORS**: Restrict to specific origins only
- [ ] **Input Validation**: Sanitize all user inputs
- [ ] **SQL Injection**: Use parameterized queries (JPA protects by default)
- [ ] **File Upload**: Validate file types, scan for malware
- [ ] **Audit Logging**: Log all security-relevant events
- [ ] **Secrets Management**: Use Vault/AWS KMS, never commit secrets

### Security Headers

```java
// SecurityConfig.java
http.headers(headers -> headers
    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
    .xssProtection(HeadersConfigurer.XXssConfig::disable)
    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
);
```

### Secrets Management

**Never commit secrets to git. Use:**

1. **Environment Variables** (Docker/Kubernetes)
2. **Docker Secrets** (Docker Swarm)
3. **HashiCorp Vault**
4. **AWS Secrets Manager / Azure Key Vault**

Example with Docker Secrets:
```yaml
# docker-compose.yml
secrets:
  jwt_secret:
    external: true
  db_password:
    external: true

services:
  backend:
    secrets:
      - jwt_secret
      - db_password
```

## Performance Tuning

### Database Optimization

```sql
-- Add indexes for common queries
CREATE INDEX idx_files_user_id ON file_metadata(user_id);
CREATE INDEX idx_files_folder_id ON file_metadata(folder_id);
CREATE INDEX idx_folders_user_id ON folders(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);

-- Analyze tables for query planner
ANALYZE users;
ANALYZE file_metadata;
ANALYZE audit_logs;
```

### Connection Pool Sizing

```
# Formula: connections = ((core_count * 2) + effective_spindle_count)
# For 4-core server with SSD:
spring.datasource.hikari.maximum-pool-size=10

# For 8-core server with SSD:
spring.datasource.hikari.maximum-pool-size=20
```

### Caching Strategy

```java
// Enable Spring Cache
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "files");
    }
}

// Use caching for expensive operations
@Cacheable(value = "users", key = "#username")
public User getUserByUsername(String username) {
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new UserNotFoundException(username));
}
```

## Backup & Disaster Recovery

### Database Backup

```bash
# Automated daily backup
pg_dump -h localhost -U postgres -d secure_cloud_storage \
  --clean --if-exists --verbose \
  > /backups/db_$(date +%Y%m%d_%H%M%S).sql

# Compress backup
gzip /backups/db_*.sql
```

### File Storage Backup

```bash
# Backup MinIO data
tar czf /backups/minio_$(date +%Y%m%d).tar.gz /var/lib/minio/data

# Or use MinIO client
mc mirror local/minio-data remote/backup-bucket
```

### Recovery Procedures

**Database Recovery:**
```bash
# Restore from backup
dropdb secure_cloud_storage
createdb secure_cloud_storage
psql -d secure_cloud_storage < backup_file.sql
```

**Point-in-Time Recovery (with WAL archiving):**
```bash
# Stop PostgreSQL
# Restore base backup
# Apply WAL logs up to target time
# Start PostgreSQL
```

## Troubleshooting

### Common Production Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| **OutOfMemoryError** | Heap too small | Increase `-Xmx`, enable G1GC |
| **Connection Pool Exhausted** | Pool size too small | Increase `maximum-pool-size` |
| **Slow Queries** | Missing indexes | Add database indexes |
| **High CPU** | Inefficient code | Profile with Actuator metrics |
| **File Upload Fails** | Max size exceeded | Increase `multipart.max-file-size` |

### Debugging Production Issues

```bash
# Enable DEBUG logging for specific package
logging.level.com.udom.securecloud.service=DEBUG

# Thread dump for analysis
jstack <pid> > threaddump.txt

# Heap dump for memory analysis
jmap -dump:format=b,file=heapdump.hprof <pid>

# GC log analysis
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log
```

## Related Documentation

- [Frontend README](../frontend/README.md) - React UI documentation
- [Root README](../README.md) - Project overview and Docker setup
- [Environment Config](../.env.example) - Environment variables template
- [Production Compose](../docker-compose.prod.yml) - Production orchestration

---

<p align="center">
  <b>Production-Ready Spring Boot Backend</b><br>
  <sub>Secure | Scalable | Observable</sub>
</p>
