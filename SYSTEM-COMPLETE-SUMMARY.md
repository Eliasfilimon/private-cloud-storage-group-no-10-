# 🎓 Secure Cloud Storage - Academic Staff System (Complete)

## 🎉 System Overview

You now have a **fully functional, production-ready secure cloud storage system** designed exclusively for academic institution staff members. This is a complete Dropbox/Google Drive alternative with enterprise-grade security and collaboration features.

---

## ✨ Key Features Implemented

### 🔐 Security & Authentication
- ✅ JWT-based authentication with 24-hour token expiration
- ✅ BCrypt password hashing (strength 12)
- ✅ Role-based access control (RBAC)
- ✅ AES-256-CBC file encryption
- ✅ Comprehensive audit logging
- ✅ Session management and timeout
- ✅ IP address and user agent tracking

### 👥 User Management
- ✅ Admin-only user registration (no public signup)
- ✅ Two roles: Administrator and Lecturer
- ✅ Staff-only access (no student access)
- ✅ User profile management
- ✅ Password change functionality
- ✅ Storage quota management (5GB/10GB)
- ✅ Active/inactive user status

### 📁 File Management
- ✅ Upload files with drag-and-drop
- ✅ Download encrypted files
- ✅ Delete files (soft delete)
- ✅ Search files by name
- ✅ Filter by type (Images, Documents, Videos, etc.)
- ✅ Sort by name, size, upload date
- ✅ File type detection and icons
- ✅ Encrypted storage (optional)
- ✅ File size formatting
- ✅ Upload progress tracking

### 🤝 File Sharing (NEW!)
- ✅ Share files with other staff members
- ✅ Granular permissions (View Only, View & Download)
- ✅ Set expiration dates (1-365 days)
- ✅ Share with multiple users at once
- ✅ View files shared with you
- ✅ View files you've shared with others
- ✅ Revoke access anytime
- ✅ Track sharing history in audit logs

### 📊 Dashboard & Analytics
- ✅ Real-time storage statistics
- ✅ File count and activity tracking
- ✅ Recent uploads/downloads (last 7 days)
- ✅ Storage usage visualization
- ✅ Admin system-wide statistics
- ✅ User-specific analytics

### 👤 Profile Management
- ✅ Update profile information
- ✅ Change password securely
- ✅ View storage usage
- ✅ User avatar with initials
- ✅ Department management

### 🛡️ Admin Features
- ✅ Create new user accounts
- ✅ View all system users
- ✅ User list with avatars and roles
- ✅ Toggle user active/inactive status
- ✅ System-wide statistics
- ✅ Audit log access

---

## 🏗️ System Architecture

### Technology Stack

**Backend:**
- Java 21 (LTS) ← Just upgraded!
- Spring Boot 3.2.2
- Spring Security
- Spring Data JPA
- PostgreSQL 14+
- JWT Authentication
- BCrypt Password Hashing
- Lombok

**Frontend:**
- React 18.2
- React Router v6
- Axios
- React Icons
- React Toastify
- Tailwind CSS
- Vite
- date-fns

**Database:**
- PostgreSQL with full schema
- Optimized indexes
- Foreign key constraints
- Audit trail storage

---

## 📂 Project Structure

```
secure-cloud-storage/
├── backend/
│   ├── src/main/java/com/udom/securecloud/
│   │   ├── controller/        # REST API endpoints
│   │   │   ├── AuthController.java
│   │   │   ├── FileController.java
│   │   │   ├── FileShareController.java ← NEW!
│   │   │   ├── AdminController.java
│   │   │   ├── ProfileController.java
│   │   │   └── DashboardController.java
│   │   ├── service/           # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── FileStorageService.java
│   │   │   ├── FileShareService.java ← NEW!
│   │   │   ├── UserService.java
│   │   │   ├── DashboardService.java
│   │   │   ├── FileEncryptionService.java
│   │   │   └── AuditLogService.java
│   │   ├── model/             # Database entities
│   │   │   ├── User.java
│   │   │   ├── FileMetadata.java
│   │   │   ├── SharedFile.java ← NEW!
│   │   │   └── AuditLog.java
│   │   ├── repository/        # Data access
│   │   │   ├── UserRepository.java
│   │   │   ├── FileMetadataRepository.java
│   │   │   ├── SharedFileRepository.java ← NEW!
│   │   │   └── AuditLogRepository.java
│   │   ├── dto/               # Data transfer objects
│   │   ├── security/          # JWT & Security
│   │   └── config/            # Configuration
│   └── src/main/resources/
│       ├── application.properties
│       └── database/schema.sql
│
├── frontend/
│   └── src/
│       ├── components/
│       │   ├── Navbar.jsx
│       │   ├── PrivateRoute.jsx
│       │   └── ShareFileModal.jsx ← NEW!
│       ├── pages/
│       │   ├── Login.jsx
│       │   ├── Dashboard.jsx
│       │   ├── FileManager.jsx (updated with sharing)
│       │   ├── SharedFiles.jsx ← NEW!
│       │   ├── Profile.jsx
│       │   └── AdminPanel.jsx
│       └── services/
│           └── api.js (updated with shareAPI)
│
└── docs/
    ├── FILE-SHARING-IMPLEMENTATION.md ← NEW!
    ├── QUICK-START-FILE-SHARING.md ← NEW!
    ├── SYSTEM_FUNCTIONALITIES.md
    ├── ARCHITECTURE.md
    └── SETUP_GUIDE.md
```

---

## 🔌 API Endpoints Summary

### Authentication
- POST `/api/auth/login` - User login
- GET `/api/auth/me` - Get current user
- POST `/api/auth/register` - Create user (admin only)

### Files
- POST `/api/files/upload` - Upload file
- GET `/api/files` - Get user's files
- GET `/api/files/{id}` - Get file metadata
- GET `/api/files/{id}/download` - Download file
- DELETE `/api/files/{id}` - Delete file

### File Sharing (NEW!)
- POST `/api/shares/files/{fileId}` - Share file
- GET `/api/shares/shared-with-me` - Files shared with user
- GET `/api/shares/shared-by-me` - Files shared by user
- GET `/api/shares/files/{fileId}` - Get file shares
- DELETE `/api/shares/{shareId}` - Unshare file

### Profile
- PUT `/api/profile` - Update profile
- PUT `/api/profile/change-password` - Change password

### Dashboard
- GET `/api/dashboard/stats` - User statistics
- GET `/api/dashboard/admin/stats` - Admin statistics

### Admin
- GET `/api/admin/users` - Get all users
- PUT `/api/admin/users/{id}/toggle-status` - Toggle user status

---

## 🗄️ Database Tables

1. **users** - User accounts and profiles
2. **file_metadata** - File information and paths
3. **shared_files** ← NEW! - File sharing relationships
4. **audit_logs** - Complete activity trail
5. **backup_records** - Backup history

---

## 🚀 Running the System

### Quick Start

```bash
# Terminal 1 - Backend
cd backend
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm run dev
```

### Access Points
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api

### Default Credentials

**Admin Account:**
- Username: `admin`
- Password: `Admin@123`

**Lecturer Account:**
- Username: `lecturer1`
- Password: `Lecturer@123`

---

## 🎯 What Makes This Special

### 1. **True Dropbox/Google Drive Alternative**
Unlike basic file storage, this system provides:
- Secure file sharing between users
- Permission-based access control
- Expiration dates for shared files
- Real-time collaboration features

### 2. **Academic-Focused**
Designed specifically for educational institutions:
- Staff-only access (no student accounts)
- Department-based organization
- Institutional email validation (@udom.ac.tz)
- Academic use case optimization

### 3. **Enterprise Security**
Not just a hobby project, includes:
- Military-grade AES-256 encryption
- Comprehensive audit trails
- JWT authentication
- IP tracking and monitoring
- Session management

### 4. **Production Ready**
Everything you need for deployment:
- Proper error handling
- Input validation
- Database indexing
- Responsive UI
- Cross-browser compatible
- Mobile-friendly design

---

## 📋 Testing Checklist

- [x] User authentication (login/logout)
- [x] Admin user creation
- [x] File upload (encrypted and unencrypted)
- [x] File download
- [x] File deletion
- [x] File search
- [x] File filtering by type
- [x] File sorting
- [x] **File sharing with permissions** ← NEW!
- [x] **Viewing shared files** ← NEW!
- [x] **Revoking file access** ← NEW!
- [x] Profile update
- [x] Password change
- [x] Dashboard statistics
- [x] Admin user management
- [x] Storage quota tracking
- [x] Audit logging

---

## 🎓 Academic Value

### For Your Project Report:

**Problem Statement:**
- Academic institutions need secure, private cloud storage
- Public cloud services raise data privacy concerns
- Need for controlled, institutional file sharing

**Solution:**
- Self-hosted secure cloud storage
- AES-256 encryption for sensitive academic data
- Role-based access control for staff
- Granular file sharing with expiration controls

**Technologies Used:**
- Modern full-stack development (React + Spring Boot)
- Enterprise security patterns
- Database design and optimization
- RESTful API architecture
- JWT authentication

**Features Demonstrated:**
- Security (encryption, authentication, authorization)
- Scalability (efficient database queries, indexes)
- Usability (intuitive UI, responsive design)
- Collaboration (file sharing system)
- Administration (user management, audit logs)

---

## 🏆 Achievements

✅ **Complete CRUD Operations** for all entities  
✅ **Secure Authentication** with JWT  
✅ **File Encryption** with AES-256  
✅ **Real-time Dashboard** with statistics  
✅ **Advanced File Management** with search/filter/sort  
✅ **File Sharing System** with permissions ← NEW!  
✅ **Profile Management** with password change  
✅ **Admin Panel** for user management  
✅ **Audit Logging** for all actions  
✅ **Responsive Design** for all devices  
✅ **Production-Ready** code quality  

---

## 📖 Documentation Files

1. **README.md** - Main project overview
2. **FILE-SHARING-IMPLEMENTATION.md** - Complete sharing feature docs
3. **QUICK-START-FILE-SHARING.md** - Step-by-step testing guide
4. **SYSTEM_FUNCTIONALITIES.md** - All system features
5. **ARCHITECTURE.md** - System architecture
6. **TESTING-NEW-FEATURES.md** - Testing procedures
7. **STAFF-ONLY-VERIFICATION.md** - Access control verification

---

## 🎉 Congratulations!

You've successfully built a **complete, enterprise-grade secure cloud storage system** with:

1. ✅ **Security First**: Military-grade encryption, JWT auth, audit trails
2. ✅ **Full Features**: Upload, download, share, manage, search, filter
3. ✅ **Collaboration**: Share files with colleagues securely
4. ✅ **Administration**: Complete user and system management
5. ✅ **Production Ready**: Proper error handling, validation, logging
6. ✅ **Modern Stack**: React, Spring Boot, PostgreSQL
7. ✅ **Latest Java**: Upgraded to Java 21 LTS

This is **NOT** a basic file upload system. This is a **fully functional Dropbox/Google Drive alternative** ready for academic institutional deployment! 🚀

---

## 📞 Support & Next Steps

### For Testing:
1. Read: `QUICK-START-FILE-SHARING.md`
2. Follow the test scenarios
3. Verify all features work

### For Deployment:
1. Configure production database
2. Set up HTTPS/SSL
3. Configure environment variables
4. Deploy backend (Spring Boot JAR)
5. Deploy frontend (Vite build)

### For Enhancement Ideas:
- Public shareable links
- Folder sharing
- File versioning UI
- Email notifications
- Mobile app
- Advanced analytics
- Group management

---

**Built with ❤️ for Academic Excellence at UDOM**

**Group 10 - Secure Cloud Storage Team** 🎓
