# SECURE SELF-HOSTED PRIVATE CLOUD STORAGE SYSTEM
## Complete System Functionalities Documentation

---

## 📋 TABLE OF CONTENTS
1. [System Overview](#system-overview)
2. [User Roles & Permissions](#user-roles--permissions)
3. [Core Functionalities](#core-functionalities)
4. [Module Specifications](#module-specifications)
5. [Security Features](#security-features)
6. [Data Flow](#data-flow)
7. [Use Cases](#use-cases)

---

## 1. SYSTEM OVERVIEW

### Purpose
A secure, self-hosted private cloud storage system designed specifically for academic institutions to safely store, manage, and share sensitive academic information. **This system is for staff use only.**

### Target Users
- **Academic Staff (Lecturers)**: Store and manage research documents, examination materials, teaching materials
- **System Administrators**: Manage users, monitor system, ensure security and compliance

**Note**: This is a staff-only system. Students do not have access.

### Key Benefits
- ✅ Full institutional control over academic data
- ✅ Enhanced data security and confidentiality
- ✅ Reduced data loss through automated backups
- ✅ Secure collaboration among staff
- ✅ Cost-effective alternative to public cloud services
- ✅ Compliance with institutional policies

---

## 2. USER ROLES & PERMISSIONS

### 👤 LECTURER (Academic Staff)
**Access Level**: Standard User

**Capabilities:**
- ✅ Login to the system
- ✅ Upload files to personal storage
- ✅ Download own files
- ✅ Organize files in folders
- ✅ View file metadata (name, size, upload date, version)
- ✅ Share files with other lecturers (permission-based)
- ✅ Delete own files
- ✅ View storage usage statistics
- ✅ Access file version history
- ✅ Update profile information
- ✅ Change password
- ✅ View personal activity logs

**Restrictions:**
- ❌ Cannot register new users
- ❌ Cannot access other users' private files
- ❌ Cannot view system-wide analytics
- ❌ Cannot modify system settings
- ❌ Cannot access admin panel

**Storage Quota**: 5 GB (default, configurable by admin)

---

### 👨‍💼 ADMINISTRATOR
**Access Level**: Full System Control

**All Lecturer capabilities PLUS:**

**User Management:**
- ✅ Register new users (lecturers and admins)
- ✅ View all system users
- ✅ Update user information
- ✅ Enable/disable user accounts
- ✅ Reset user passwords
- ✅ Modify user storage quotas
- ✅ Assign/change user roles
- ✅ Delete user accounts

**System Monitoring:**
- ✅ View system-wide storage statistics
- ✅ Monitor active users and sessions
- ✅ View total files and storage usage
- ✅ Access all audit logs (system-wide)
- ✅ Track file access patterns
- ✅ Monitor security events

**Security & Compliance:**
- ✅ Configure backup schedules
- ✅ Restore from backups
- ✅ Review security logs
- ✅ Monitor failed login attempts
- ✅ View file access audit trails
- ✅ Generate compliance reports

**Analytics & Reporting:**
- ✅ Generate storage usage reports
- ✅ Create user activity reports
- ✅ Export audit logs
- ✅ View system performance metrics
- ✅ Analyze data trends

**System Configuration:**
- ✅ Configure file upload limits
- ✅ Set global storage policies
- ✅ Manage backup settings
- ✅ Configure security settings

**Storage Quota**: 10 GB (default, configurable)

---

## 3. CORE FUNCTIONALITIES

### 🔐 A. AUTHENTICATION & AUTHORIZATION

#### Login System
**Process:**
1. User enters username and password
2. System validates credentials
3. System generates JWT token (24-hour expiration)
4. Token stored in browser localStorage
5. User redirected to dashboard

**Security Features:**
- Password hashing with BCrypt
- Session timeout after inactivity
- Failed login attempt tracking
- IP address logging
- Secure password requirements (min 6 characters)

#### Access Control
- Role-Based Access Control (RBAC)
- Route protection (authenticated routes)
- API endpoint authorization
- Resource-level permissions

#### User Registration (Admin Only)
**Process:**
1. Admin navigates to Admin Panel
2. Clicks "Add New User"
3. Fills in user details:
   - Full name
   - Username (unique)
   - Email (unique, preferably @udom.ac.tz)
   - Password
   - Role (Lecturer/Admin)
4. System creates account
5. User can login immediately

**Validations:**
- Username uniqueness check
- Email format validation
- Password strength requirements
- Duplicate email prevention

---

### 📁 B. FILE STORAGE & MANAGEMENT

#### File Upload
**Capabilities:**
- Single file upload
- Multiple file upload (batch)
- Drag-and-drop interface
- Progress tracking
- File size limit: 100 MB per file
- Support for all file types

**Process:**
1. User selects file(s)
2. System validates file size and quota
3. File encrypted using AES-256
4. Checksum calculated (integrity verification)
5. Metadata stored in database
6. Encrypted file saved to server
7. User storage quota updated
8. Audit log created

**Metadata Stored:**
- File ID (unique)
- Original filename
- File path (encrypted location)
- MIME type
- File size (bytes)
- Owner ID
- Folder/category
- Upload timestamp
- Checksum (SHA-256)
- Encryption status
- Version number

#### File Organization
**Features:**
- Folder/directory structure
- File categorization
- Search functionality
- Sort by: name, date, size, type
- Filter by: file type, date range, size
- Breadcrumb navigation

#### File Download
**Process:**
1. User clicks download button
2. System verifies permissions
3. File decrypted in memory
4. Original filename restored
5. File sent to user's browser
6. Download logged in audit trail

**Security:**
- Permission verification
- Secure file retrieval
- No temporary unencrypted files
- Audit logging

#### File Deletion
**Process:**
1. User selects file(s) to delete
2. Confirmation dialog
3. File marked as deleted
4. Soft delete (30-day recovery period)
5. After 30 days, permanent deletion
6. Storage quota freed
7. Audit log created

**Features:**
- Confirmation required
- Soft delete with recovery
- Bulk deletion
- Audit trail

#### File Versioning
**Capabilities:**
- Automatic version tracking
- Upload new version of existing file
- View version history
- Restore previous version
- Version comparison
- Version metadata

**Version Metadata:**
- Version number
- Upload date/time
- File size
- Uploader
- Change notes (optional)

---

### 🔒 C. SECURITY MODULE

#### Data Encryption

**At Rest (Stored Files):**
- Algorithm: AES-256-CBC
- Key management: Environment variable
- All files encrypted before storage
- Metadata stored separately
- Encrypted filenames

**In Transit (Network):**
- TLS/HTTPS protocol
- Secure API communication
- Certificate validation
- Encrypted request/response

#### Access Control
**Mechanisms:**
- JWT token authentication
- Role-based permissions
- Resource ownership validation
- API endpoint protection
- Session management

#### Audit Logging
**All Activities Tracked:**
- User login/logout
- File upload
- File download
- File deletion
- File sharing
- User creation (admin)
- Password changes
- Failed login attempts
- Unauthorized access attempts

**Log Information:**
- User ID and username
- Action performed
- Timestamp
- IP address
- File involved (if applicable)
- Status (success/failure)
- Additional details

**Log Storage:**
- Stored in secure database
- Searchable and filterable
- Export capability
- Retention policy configurable

#### Security Monitoring
**Features:**
- Real-time security alerts
- Failed login tracking
- Suspicious activity detection
- IP-based access monitoring
- Session anomaly detection

---

### 💾 D. BACKUP & RECOVERY MODULE

#### Automated Backups
**Schedule:**
- Daily incremental backups
- Weekly full backups
- Configurable schedule
- Off-peak hours execution

**Backup Contents:**
- Database (PostgreSQL)
- All encrypted files
- System configuration
- User metadata

**Backup Storage:**
- Separate backup directory
- Versioned backups
- Retention policy: 30 days
- Compression enabled

**Process:**
1. Scheduled job triggers
2. Database dump created
3. Files copied to backup location
4. Checksum verification
5. Old backups pruned
6. Backup log created
7. Admin notification (if configured)

#### Data Recovery
**Capabilities:**
- Full system restore
- Individual file restore
- User data restore
- Point-in-time recovery

**Process:**
1. Admin selects backup point
2. System validation
3. Data restored from backup
4. Integrity verification
5. Users notified
6. Recovery audit log

#### Disaster Recovery
**Features:**
- Automated recovery procedures
- Backup integrity checks
- Recovery testing capability
- Documentation

---

### 📊 E. REPORTING & ANALYTICS MODULE

#### Storage Analytics
**Metrics:**
- Total storage used (system-wide)
- Storage per user
- Storage by file type
- Storage trends over time
- Quota utilization
- Free space available

**Visualizations:**
- Pie charts (storage by user/type)
- Line graphs (usage trends)
- Bar charts (user comparison)
- Progress bars (quota usage)

#### Activity Reports
**Data Points:**
- File uploads per day/week/month
- File downloads per day/week/month
- Active users
- Peak usage times
- Most accessed files
- User activity patterns

#### User Reports
**Information:**
- User list with roles
- Storage usage per user
- Last login dates
- Account status
- File count per user
- Quota compliance

#### Security Reports
**Components:**
- Failed login attempts
- Unauthorized access attempts
- Password changes
- User account changes
- Suspicious activities
- Security events timeline

#### Export Capabilities
**Formats:**
- PDF reports
- CSV data export
- Excel spreadsheets
- JSON (API)

**Scheduling:**
- Generate on-demand
- Scheduled reports
- Email delivery (optional)

---

### 🤝 F. FILE SHARING & COLLABORATION

#### Sharing Mechanisms
**Types:**
1. **Internal Sharing** (User-to-User)
   - Share with specific users
   - Permission levels:
     - View only
     - Download
     - Edit (future)
   - Expiration dates
   - Revoke access anytime

2. **Link Sharing** (Future)
   - Generate secure link
   - Password protection
   - Expiration date
   - Access limits
   - Track link usage

#### Permission Management
**Granular Controls:**
- Owner can set permissions
- View-only access
- Download access
- Modify access (future)
- Time-limited access
- Revoke anytime

#### Sharing Audit
**Tracking:**
- Who shared what
- With whom
- When shared
- Access history
- Permission changes
- Share revocations

---

### 👤 G. PROFILE MANAGEMENT

#### User Profile
**Information:**
- Full name
- Username
- Email address
- Role
- Storage quota
- Storage used
- Account creation date
- Last login

#### Profile Updates
**Editable Fields:**
- Full name
- Email address
- Profile picture (future)

**Non-Editable:**
- Username (set at creation)
- Role (admin only)
- Storage quota (admin only)

#### Password Management
**Features:**
- Change password
- Current password verification
- Password strength indicator
- Password confirmation
- Secure password hashing

---

## 4. MODULE SPECIFICATIONS

### Module 1: User Management
**Components:**
- User registration (admin only)
- User authentication
- Role assignment
- User profile management
- Password management
- User status management

**Database Tables:**
- `users`

**Key Functions:**
- `register()` - Create new user
- `login()` - Authenticate user
- `updateProfile()` - Update user info
- `changePassword()` - Update password
- `enableUser()` - Enable/disable account
- `deleteUser()` - Remove user

---

### Module 2: File Storage
**Components:**
- File upload handler
- File download handler
- File organization
- File versioning
- Metadata management
- Storage quota enforcement

**Database Tables:**
- `files` (file_metadata)
- `file_versions`
- `folders`

**Key Functions:**
- `uploadFile()` - Upload and encrypt file
- `downloadFile()` - Decrypt and serve file
- `deleteFile()` - Remove file
- `listFiles()` - Get user files
- `searchFiles()` - Search functionality
- `createFolder()` - Create directory
- `checkQuota()` - Verify storage limits

---

### Module 3: Security
**Components:**
- Encryption service
- Authentication service
- Authorization middleware
- Audit logging
- Security monitoring

**Database Tables:**
- `audit_logs`
- `sessions`

**Key Functions:**
- `encryptFile()` - Encrypt file data
- `decryptFile()` - Decrypt file data
- `hashPassword()` - Hash passwords
- `generateToken()` - Create JWT
- `verifyToken()` - Validate JWT
- `logActivity()` - Record audit log
- `checkPermission()` - Verify access

---

### Module 4: Backup & Recovery
**Components:**
- Backup scheduler
- Backup executor
- Recovery manager
- Backup verification

**Database Tables:**
- `backup_records`

**Key Functions:**
- `scheduleBackup()` - Set backup schedule
- `executeBackup()` - Run backup
- `verifyBackup()` - Check integrity
- `listBackups()` - Show available backups
- `restoreBackup()` - Recover data

---

### Module 5: Reporting & Analytics
**Components:**
- Data aggregation
- Report generation
- Visualization
- Export functionality

**Database Tables:**
- Views and aggregations

**Key Functions:**
- `getStorageStats()` - Storage metrics
- `getUserActivity()` - User statistics
- `generateReport()` - Create report
- `exportData()` - Export to file
- `getAuditLogs()` - Retrieve logs

---

### Module 6: File Sharing
**Components:**
- Share management
- Permission control
- Access tracking

**Database Tables:**
- `shared_files`
- `file_permissions`

**Key Functions:**
- `shareFile()` - Share with user
- `unshareFile()` - Revoke access
- `getSharedFiles()` - List shared
- `checkSharePermission()` - Verify access

---

## 5. SECURITY FEATURES

### CIA Triad Implementation

#### Confidentiality
- ✅ AES-256 encryption for files
- ✅ BCrypt password hashing
- ✅ JWT token authentication
- ✅ TLS/HTTPS communication
- ✅ Access control mechanisms
- ✅ Private data segregation

#### Integrity
- ✅ File checksums (SHA-256)
- ✅ Database constraints
- ✅ Input validation
- ✅ Audit logging
- ✅ Version control
- ✅ Backup verification

#### Availability
- ✅ Automated backups
- ✅ Disaster recovery
- ✅ System monitoring
- ✅ Error handling
- ✅ Redundancy
- ✅ High uptime design

---

### Security Best Practices

1. **Authentication**
   - Strong password policy
   - Session management
   - Token expiration
   - Failed login lockout (future)

2. **Authorization**
   - Role-based access
   - Resource ownership
   - Principle of least privilege
   - Permission verification

3. **Data Protection**
   - Encryption at rest and in transit
   - Secure key management
   - Data sanitization
   - SQL injection prevention

4. **Monitoring**
   - Comprehensive audit logs
   - Security event tracking
   - Real-time alerts (future)
   - Regular security audits

5. **Compliance**
   - Data retention policies
   - Access control policies
   - Audit trail requirements
   - Privacy protection

---

## 6. DATA FLOW

### File Upload Flow
```
User → Frontend → API (Auth) → Validation → Encryption → Storage → Database → Audit Log → Response → User
```

### File Download Flow
```
User → Frontend → API (Auth) → Permission Check → Database Lookup → Decrypt File → Stream to User → Audit Log
```

### User Authentication Flow
```
User → Login Form → API → Database Query → Password Verify → JWT Generate → Store Token → Dashboard
```

### Admin User Creation Flow
```
Admin → Admin Panel → Add User Form → API (Auth + Admin Check) → Validate → Hash Password → Create User → Database → Notification → Admin
```

---

## 7. USE CASES

### Use Case 1: Lecturer Uploads Examination Paper
1. Lecturer logs in
2. Navigates to File Manager
3. Clicks "Upload File"
4. Selects exam paper (PDF)
5. System encrypts and stores file
6. File appears in lecturer's file list
7. Audit log records upload
8. Storage quota updated

### Use Case 2: Lecturer Shares Research Document
1. Lecturer selects document
2. Clicks "Share" button
3. Enters colleague's username
4. Sets permission (View/Download)
5. Sets expiration date (optional)
6. System creates share record
7. Colleague receives notification
8. Colleague can access shared file
9. All access logged

### Use Case 3: Admin Creates New User Account
1. Admin logs in
2. Goes to Admin Panel
3. Clicks "Add New User"
4. Enters user details:
   - Name: Dr. John Doe
   - Username: jdoe
   - Email: jdoe@udom.ac.tz
   - Password: SecurePass123
   - Role: Lecturer
5. System validates and creates account
6. New user can login immediately
7. Welcome email sent (future)

### Use Case 4: System Performs Automated Backup
1. Scheduled time reached (e.g., 2 AM)
2. Backup job triggered
3. Database dumped
4. Files copied to backup location
5. Compression applied
6. Checksum calculated
7. Old backups pruned
8. Backup record created
9. Admin notified (if configured)

### Use Case 5: Admin Generates Storage Report
1. Admin navigates to Admin Panel
2. Clicks "Storage Report"
3. Selects date range
4. System aggregates data:
   - Total storage used
   - Storage per user
   - File type distribution
5. Visualizations generated
6. Report displayed
7. Admin exports to PDF
8. Report saved/emailed

### Use Case 6: Lecturer Restores Deleted File
1. Lecturer accidentally deletes file
2. Realizes mistake within 30 days
3. Contacts admin
4. Admin views deleted files
5. Admin restores file
6. File reappears in lecturer's storage
7. Storage quota adjusted
8. Restoration logged

---

## 8. SYSTEM CONSTRAINTS & LIMITS

### Storage Limits
- **Lecturer**: 5 GB (default)
- **Admin**: 10 GB (default)
- **Max file size**: 100 MB per file
- **Total system**: Based on server capacity

### Performance Limits
- **Concurrent users**: 50+ (scalable)
- **API rate limit**: 100 requests/minute/user
- **Upload speed**: Network dependent
- **Download speed**: Network dependent

### Security Constraints
- **Password**: Minimum 6 characters
- **Session**: 24-hour token expiration
- **Failed logins**: Track and alert
- **Backup retention**: 30 days

---

## 9. FUTURE ENHANCEMENTS

### Phase 2 Features
- [ ] Real-time collaboration on documents
- [ ] Advanced file sharing with public links
- [ ] Mobile application
- [ ] Email notifications
- [ ] Advanced search (content search)
- [ ] File preview (PDF, images, documents)
- [ ] Bulk file operations
- [ ] Two-factor authentication (2FA)
- [ ] LDAP/Active Directory integration
- [ ] API for third-party integrations

### Phase 3 Features
- [ ] AI-powered file categorization
- [ ] Automated malware scanning
- [ ] Advanced analytics with ML
- [ ] Video storage and streaming
- [ ] Integration with learning management systems
- [ ] Cloud storage synchronization
- [ ] Advanced access policies
- [ ] Compliance reporting automation

---

## 10. TECHNICAL SPECIFICATIONS

### Backend (Spring Boot)
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.2
- **Database**: PostgreSQL 14+
- **Authentication**: JWT
- **Encryption**: AES-256-CBC
- **Hashing**: BCrypt

### Frontend (React)
- **Framework**: React 18
- **Router**: React Router v6
- **Styling**: Tailwind CSS
- **Build Tool**: Vite
- **HTTP Client**: Axios
- **UI Icons**: React Icons

### Infrastructure
- **Server OS**: Ubuntu Server
- **Web Server**: Nginx (reverse proxy)
- **SSL/TLS**: Let's Encrypt
- **Backup**: Automated scripts
- **Monitoring**: Spring Actuator

---

## 11. DEPLOYMENT ARCHITECTURE

### Development Environment
```
Frontend (localhost:3000) → Backend API (localhost:8080) → PostgreSQL (localhost:5432)
```

### Production Environment
```
Internet → Nginx (80/443) → Spring Boot (8080) → PostgreSQL (5432)
                ↓
          File Storage (/uploads)
                ↓
          Backup Storage (/backups)
```

---

## CONCLUSION

This system provides a comprehensive, secure, and user-friendly solution for academic institutions to manage sensitive academic data. The modular architecture ensures scalability, maintainability, and the ability to add future enhancements.

**Key Success Factors:**
- ✅ Strong security implementation
- ✅ User-friendly interface
- ✅ Comprehensive audit trails
- ✅ Reliable backup system
- ✅ Role-based access control
- ✅ Institutional data sovereignty

---

**Document Version**: 1.0  
**Last Updated**: February 16, 2026  
**Prepared By**: Group 10 - UDOM Final Year Project
