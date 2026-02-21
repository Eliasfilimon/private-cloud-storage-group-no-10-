# Complete Setup Guide

Step-by-step guide to set up the Secure Cloud Storage System.

## Prerequisites

### Required Software
1. **Java JDK 17+**
   ```bash
   java -version
   # Should show Java 17 or higher
   ```

2. **Node.js 18+ and npm**
   ```bash
   node --version
   npm --version
   ```

3. **PostgreSQL 14+**
   ```bash
   psql --version
   ```

4. **Git**
   ```bash
   git --version
   ```

## Database Setup

### 1. Install PostgreSQL (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Create Database and User
```bash
sudo -u postgres psql

# In PostgreSQL shell:
CREATE DATABASE secure_cloud_db;
CREATE USER cloud_user WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE secure_cloud_db TO cloud_user;
\q
```

### 3. Test Connection
```bash
psql -h localhost -U cloud_user -d secure_cloud_db
```

## Backend Setup

### 1. Navigate to Backend Directory
```bash
cd secure-cloud-storage/backend
```

### 2. Configure Database Connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/secure_cloud_db
spring.datasource.username=cloud_user
spring.datasource.password=your_secure_password
```

### 3. Generate JWT Secret
```bash
# Generate a secure random key
openssl rand -base64 32
```

Update `jwt.secret` in `application.properties` with the generated key.

### 4. Build and Run
```bash
# Using Maven wrapper (recommended)
./mvnw clean install
./mvnw spring-boot:run

# Or if Maven is installed globally
mvn clean install
mvn spring-boot:run
```

### 5. Verify Backend is Running
Open browser: `http://localhost:8080`

You should see a response from the API.

## Frontend Setup

### 1. Navigate to Frontend Directory
```bash
cd ../frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Create Environment File
```bash
cp .env.example .env
```

Edit `.env`:
```env
VITE_API_URL=http://localhost:8080/api
```

### 4. Run Development Server
```bash
npm run dev
```

### 5. Access Application
Open browser: `http://localhost:3000`

## First-Time Setup

### 1. Create Admin Account
Create the first admin account directly in the database:

```sql
-- Connect to database
psql -h localhost -U cloud_user -d secure_cloud_db

-- Insert admin user (password: admin123)
-- Note: You'll need to generate a proper BCrypt hash
INSERT INTO users (username, email, password, full_name, role, enabled, storage_quota, storage_used, created_at, updated_at)
VALUES (
  'admin',
  'admin@udom.ac.tz',
  '$2a$10$rRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR', -- BCrypt hash for 'admin123'
  'System Administrator',
  'ADMIN',
  true,
  10737418240, -- 10 GB
  0,
  NOW(),
  NOW()
);
```

**Important**: After creating the first admin account, use the admin panel to add other users. Public registration is disabled for security.

### 2. Create Storage Directories
```bash
cd secure-cloud-storage/backend
mkdir -p uploads backups logs
chmod 755 uploads backups logs
```

## Testing the System

### 1. Create First Admin User
Create the admin account using the SQL command above.

### 2. Login as Admin
1. Go to `http://localhost:3000/login`
2. Enter admin credentials
3. Click "Sign in"

### 3. Add New Users (Admin Only)
1. Navigate to Admin Panel
2. Click "Add New User" button
3. Fill in user details (name, username, email, password, role)
4. Click "Create User"

### 4. Login as Regular User
1. Logout from admin account
2. Login with the newly created user credentials

### 5. Upload a Test File
1. Navigate to Dashboard
2. Click "Upload File"
3. Select a file and upload

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Find process using port 8080
sudo lsof -i :8080
# Kill the process
sudo kill -9 <PID>
```

**Database connection failed:**
- Check PostgreSQL is running: `sudo systemctl status postgresql`
- Verify credentials in `application.properties`
- Check database exists: `psql -U postgres -l`

### Frontend Issues

**Port 3000 already in use:**
Edit `vite.config.js` to change port:
```javascript
server: {
  port: 3001,
  // ...
}
```

**API connection failed:**
- Verify backend is running on port 8080
- Check CORS settings in backend
- Verify `VITE_API_URL` in `.env`

## Production Deployment

See `docs/DEPLOYMENT.md` for production deployment instructions.

## Next Steps

1. Configure file encryption settings
2. Set up automated backups
3. Configure SSL/TLS certificates
4. Set up monitoring and logging
5. Implement additional security measures

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Project Wiki](https://github.com/your-repo/wiki)
