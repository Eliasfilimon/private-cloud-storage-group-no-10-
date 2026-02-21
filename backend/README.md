# Backend - Secure Cloud Storage API

Spring Boot REST API for secure cloud storage system.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/tz/ac/udom/cloudstorage/
│   │   │   ├── SecureCloudStorageApplication.java
│   │   │   ├── config/          # Security, CORS, File storage config
│   │   │   ├── controller/      # REST API endpoints
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Database access
│   │   │   ├── model/           # Entity classes
│   │   │   ├── dto/             # Data transfer objects
│   │   │   ├── security/        # JWT, Authentication
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── test/                    # Unit and integration tests
├── pom.xml
└── README.md
```

## Setup Instructions

### 1. Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+

### 2. Database Setup
```sql
CREATE DATABASE secure_cloud_db;
CREATE USER cloud_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE secure_cloud_db TO cloud_user;
```

### 3. Configuration
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/secure_cloud_db
spring.datasource.username=cloud_user
spring.datasource.password=your_secure_password
jwt.secret=change-this-to-a-secure-secret-key
```

### 4. Run Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven
mvn spring-boot:run

# Package as JAR
mvn clean package
java -jar target/secure-cloud-storage-1.0.0.jar
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user (Admin only)
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user info

### File Management
- `POST /api/files/upload` - Upload file
- `GET /api/files` - List user files
- `GET /api/files/{id}` - Get file metadata
- `GET /api/files/{id}/download` - Download file
- `DELETE /api/files/{id}` - Delete file
- `PUT /api/files/{id}` - Update file metadata

### User Management (Admin)
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Analytics & Reports
- `GET /api/reports/storage` - Storage usage report
- `GET /api/reports/activity` - User activity report
- `GET /api/audit-logs` - Audit logs

## Security Features
- JWT-based authentication
- Role-based access control (RBAC)
- AES-256 file encryption
- Password hashing with BCrypt
- CORS protection
- SQL injection prevention
- XSS protection

## Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

## Deployment
See deployment guide in `/docs/deployment.md`
