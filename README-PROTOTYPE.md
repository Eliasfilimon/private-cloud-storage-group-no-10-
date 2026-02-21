# Secure Cloud Storage - Complete Prototype Setup

**Staff-Only Cloud Storage System for University of Dodoma**

This guide will help you run the complete working prototype of the Secure Cloud Storage system designed exclusively for university staff (administrators and lecturers).

## System Overview

This is a **staff-only** secure cloud storage solution with role-based access:
- **Administrators**: Full system control, user management, analytics
- **Lecturers**: Personal file storage and management

## Prerequisites

- **Java 17** or higher
- **Node.js 18** or higher
- **PostgreSQL 14** or higher
- **Maven 3.8** or higher
- **Git**

## Quick Start (Linux/MacOS)

### 1. Setup Database

```bash
cd backend
chmod +x setup-database.sh
./setup-database.sh
```

### 2. Start Backend

```bash
# Build the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

### 3. Start Frontend

```bash
# Open new terminal
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on **http://localhost:5173**

## Manual Database Setup (Windows or if script fails)

### Create Database

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database
CREATE DATABASE secure_cloud_storage;

-- Connect to the new database
\c secure_cloud_storage

-- Run the schema file
\i backend/src/main/resources/database/schema.sql

-- Exit
\q
```

### Create Storage Directories

```bash
cd backend
mkdir uploads backups
```

## Default Staff Accounts

| Role | Username | Password | Storage Quota |
|------|----------|----------|---------------|
| Admin | admin | Admin@123 | 10 GB |
| Lecturer | lecturer1 | Lecturer@123 | 5 GB |

**Note**: This system is for staff only. Only administrators can create new user accounts.

## Testing the Prototype

### 1. Login
- Go to http://localhost:5173
- Login with admin credentials

### 2. Create User (Admin Only)
- Click on "Admin Panel"
- Click "Create New User"
- Fill in the form and submit

### 3. Upload Files
- Go to "File Manager"
- Click "Upload File"
- Select a file and choose encryption option
- Upload

### 4. Download Files
- View your files in the File Manager
- Click download icon to download (files are automatically decrypted)

### 5. Delete Files
- Click delete icon on any file
- Confirm deletion

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Get current user info
- `POST /api/auth/register` - Create new user (Admin only)

### Files
- `POST /api/files/upload` - Upload file
- `GET /api/files` - Get user's files
- `GET /api/files/{id}/download` - Download file
- `DELETE /api/files/{id}` - Delete file

### Admin
- `GET /api/admin/users` - Get all users
- `GET /api/admin/users/{id}` - Get user by ID
- `PUT /api/admin/users/{id}/toggle-status` - Activate/Deactivate user
- `DELETE /api/admin/users/{id}` - Delete user

## Features Implemented

✅ **User Authentication**
- JWT-based authentication
- BCrypt password hashing
- Session management

✅ **File Management**
- File upload with encryption (AES-256-CBC)
- File download with automatic decryption
- File deletion (soft delete)
- Storage quota management

✅ **User Management**
- Admin can create staff accounts (lecturers and administrators)
- Staff-only role-based access control (ADMIN, LECTURER)
- User activation/deactivation
- User deletion

✅ **Security**
- File encryption at rest
- Secure JWT tokens
- Password strength validation
- CORS protection

✅ **Audit Logging**
- All user actions are logged
- IP address and user agent tracking
- Comprehensive audit trail

✅ **UI/UX**
- UDOM brand colors (Blue #0047AB, Gold #FFD700)
- Responsive design
- Modern interface with Tailwind CSS

## Troubleshooting

### Backend won't start

**Issue**: Database connection error
```
Solution: Check PostgreSQL is running
sudo systemctl status postgresql
sudo systemctl start postgresql
```

**Issue**: Port 8080 already in use
```
Solution: Change port in application.properties
server.port=8081
```

### Frontend won't start

**Issue**: Port 5173 already in use
```
Solution: The dev server will automatically try another port
```

**Issue**: Cannot connect to backend
```
Solution: Verify backend is running on port 8080
Check vite.config.js proxy settings
```

### File upload fails

**Issue**: File too large
```
Solution: Check spring.servlet.multipart.max-file-size in application.properties
```

**Issue**: Storage quota exceeded
```
Solution: Login as admin and check user's storage quota
```

## Project Structure

```
secure-cloud-storage/
├── backend/
│   ├── src/main/java/com/udom/securecloud/
│   │   ├── config/          # Security configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/       # Exception handlers
│   │   ├── model/          # JPA entities
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # JWT & Security classes
│   │   └── service/        # Business logic
│   ├── src/main/resources/
│   │   ├── database/       # SQL schema
│   │   └── application.properties
│   ├── setup-database.sh   # Database setup script
│   └── pom.xml
│
└── frontend/
    ├── src/
    │   ├── components/     # React components
    │   ├── pages/         # Page components
    │   ├── services/      # API services
    │   └── App.jsx
    ├── package.json
    └── vite.config.js
```

## Next Steps for Production

1. **Change JWT Secret**: Generate strong secret in application.properties
2. **Update Database Password**: Change PostgreSQL password
3. **Configure HTTPS**: Set up SSL certificates
4. **Environment Variables**: Use environment variables for sensitive data
5. **Backup Strategy**: Implement automated backups
6. **Monitoring**: Add application monitoring and logging
7. **Testing**: Run comprehensive test suite

## Demo Scenarios

### Scenario 1: Admin Creates Lecturer Account
1. Login as admin
2. Navigate to Admin Panel
3. Create new lecturer with username, email, password
4. Verify lecturer can login

### Scenario 2: Lecturer Uploads Encrypted File
1. Login as lecturer
2. Go to File Manager
3. Upload file with encryption enabled
4. Download file and verify it's decrypted correctly

### Scenario 3: Storage Quota Management
1. Upload files until quota is reached
2. Verify error message when quota exceeded
3. Delete files to free up space

### Scenario 4: Audit Trail
1. Perform various actions (upload, download, delete)
2. Check database audit_logs table
3. Verify all actions are logged

## Support

For issues or questions:
- Check the troubleshooting section
- Review application logs
- Check database connection
- Verify all services are running

---

**Group 10 - UDOM CSE**  
**Secure Self-Hosted Private Cloud Storage for Academic Institutions**
