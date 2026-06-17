# API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All endpoints (except `/auth/**` and `/actuator/health`) require JWT token in Authorization header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## Authentication Endpoints

### 1. Login
**Endpoint**: `POST /auth/login`

**Request**:
```json
{
  "username": "user@udom.ac.tz",
  "password": "YourPassword123!"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "user@udom.ac.tz",
  "email": "user@udom.ac.tz",
  "fullName": "John Doe",
  "role": "STAFF",
  "department": "IT",
  "storageQuota": 5368709120,
  "storageUsed": 1073741824,
  "mustChangePassword": false
}
```

**Error** (401 Unauthorized):
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

---

### 2. Register
**Endpoint**: `POST /auth/register`

**Request**:
```json
{
  "email": "newuser@udom.ac.tz",
  "firstName": "Jane",
  "lastName": "Smith",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!"
}
```

**Response** (201 Created):
```json
{
  "id": 2,
  "username": "newuser@udom.ac.tz",
  "email": "newuser@udom.ac.tz",
  "fullName": "Jane Smith",
  "role": "STAFF",
  "storageQuota": 5368709120,
  "storageUsed": 0,
  "isActive": true,
  "createdAt": "2026-06-04T10:00:00",
  "lastLogin": null,
  "totpEnabled": false
}
```

---

## Admin Endpoints

### 1. List All Users
**Endpoint**: `GET /admin/users`

**Query Parameters**:
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `search` (string, optional) - Search by username or email

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin@udom.ac.tz",
      "email": "admin@udom.ac.tz",
      "fullName": "System Administrator",
      "role": "ADMIN",
      "department": "IT",
      "isActive": true,
      "createdAt": "2026-06-04T10:00:00",
      "lastLogin": "2026-06-04T11:00:00",
      "storageQuota": 10737418240,
      "storageUsed": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

---

### 2. Create User
**Endpoint**: `POST /admin/users`

**Request**:
```json
{
  "email": "newuser@udom.ac.tz",
  "firstName": "John",
  "lastName": "Doe",
  "role": "STAFF",
  "department": "HR"
}
```

**Response** (201 Created):
```json
{
  "id": 3,
  "username": "newuser@udom.ac.tz",
  "email": "newuser@udom.ac.tz",
  "fullName": "John Doe",
  "role": "STAFF",
  "department": "HR",
  "storageQuota": 5368709120,
  "isActive": true,
  "createdAt": "2026-06-04T10:00:00"
}
```

**Note**: Default password is user's last name in uppercase (e.g., "DOE"). User must change password on first login.

---

### 3. Reset User Password
**Endpoint**: `POST /admin/users/{userId}/reset-password`

**Path Parameters**:
- `userId` (long) - User ID

**Response** (200 OK):
```json
{
  "message": "Password reset successfully",
  "userId": 3,
  "username": "newuser@udom.ac.tz",
  "tempPassword": "DOE",
  "mustChangePassword": true,
  "note": "User must change password on next login"
}
```

---

### 4. Update User Role
**Endpoint**: `PUT /admin/users/{userId}/role`

**Path Parameters**:
- `userId` (long) - User ID

**Request**:
```json
{
  "role": "ADMIN"
}
```

**Response** (200 OK):
```json
{
  "message": "User role updated successfully",
  "userId": 3,
  "newRole": "ADMIN"
}
```

---

### 5. Toggle User Status
**Endpoint**: `PUT /admin/users/{userId}/status`

**Path Parameters**:
- `userId` (long) - User ID

**Request**:
```json
{
  "isActive": false
}
```

**Response** (200 OK):
```json
{
  "message": "User status updated successfully",
  "userId": 3,
  "isActive": false
}
```

---

### 6. Delete User
**Endpoint**: `DELETE /admin/users/{userId}`

**Path Parameters**:
- `userId` (long) - User ID

**Response** (204 No Content)

---

### 7. Get Audit Logs
**Endpoint**: `GET /admin/audit-logs`

**Query Parameters**:
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `action` (string, optional) - Filter by action (USER_LOGIN, USER_CREATE, FILE_UPLOAD, etc.)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "userId": 1,
      "username": "admin@udom.ac.tz",
      "action": "USER_LOGIN",
      "resourceType": "USER",
      "resourceId": 1,
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "status": "SUCCESS",
      "details": "User logged in successfully",
      "createdAt": "2026-06-04T11:00:00"
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "currentPage": 0
}
```

---

## File Endpoints

### 1. Upload File
**Endpoint**: `POST /files/upload`

**Request**:
- Content-Type: `multipart/form-data`
- `file` (file) - File to upload
- `folderId` (long, optional) - Folder ID

**Response** (201 Created):
```json
{
  "id": 1,
  "fileName": "document.pdf",
  "originalName": "document.pdf",
  "fileSize": 1048576,
  "mimeType": "application/pdf",
  "isEncrypted": true,
  "createdAt": "2026-06-04T10:00:00",
  "updatedAt": "2026-06-04T10:00:00"
}
```

---

### 2. Download File
**Endpoint**: `GET /files/{fileId}/download`

**Path Parameters**:
- `fileId` (long) - File ID

**Response**: File content (binary)

---

### 3. Delete File
**Endpoint**: `DELETE /files/{fileId}`

**Path Parameters**:
- `fileId` (long) - File ID

**Response** (204 No Content)

---

### 4. List Files
**Endpoint**: `GET /files`

**Query Parameters**:
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `folderId` (long, optional) - Filter by folder

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "fileName": "document.pdf",
      "originalName": "document.pdf",
      "fileSize": 1048576,
      "mimeType": "application/pdf",
      "isEncrypted": true,
      "createdAt": "2026-06-04T10:00:00"
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "currentPage": 0
}
```

---

## Profile Endpoints

### 1. Get Current User Profile
**Endpoint**: `GET /profile/me`

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "user@udom.ac.tz",
  "email": "user@udom.ac.tz",
  "fullName": "John Doe",
  "role": "STAFF",
  "department": "IT",
  "storageQuota": 5368709120,
  "storageUsed": 1073741824,
  "isActive": true,
  "createdAt": "2026-06-04T10:00:00",
  "lastLogin": "2026-06-04T11:00:00",
  "totpEnabled": false
}
```

---

### 2. Update Profile
**Endpoint**: `PUT /profile/update`

**Request**:
```json
{
  "firstName": "Jane",
  "lastName": "Doe",
  "department": "HR"
}
```

**Response** (200 OK):
```json
{
  "message": "Profile updated successfully",
  "user": {
    "id": 1,
    "username": "user@udom.ac.tz",
    "fullName": "Jane Doe",
    "department": "HR"
  }
}
```

---

### 3. Change Password
**Endpoint**: `POST /profile/change-password`

**Request**:
```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

**Response** (200 OK):
```json
{
  "message": "Password changed successfully"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input"
}
```

### 401 Unauthorized
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 500 Internal Server Error
```json
{
  "timestamp": "2026-06-04T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Rate Limiting

- **Login**: 5 attempts per minute per IP
- **Register**: 3 attempts per hour per IP
- **Admin Operations**: 20 requests per minute per user
- **File Operations**: 100 requests per minute per user

---

## Swagger/OpenAPI

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

---

**Last Updated**: June 4, 2026
**Version**: 1.0
