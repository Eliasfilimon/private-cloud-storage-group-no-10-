# PROTOTYPE DEVELOPMENT PLAN
## Secure Self-Hosted Private Cloud Storage System

---

## 📋 PROTOTYPE OBJECTIVES

### What is a Prototype?
A **working demonstration** of the system that includes core functionalities to validate the concept and gather feedback.

### Goals:
1. ✅ Demonstrate key features to supervisors/stakeholders
2. ✅ Validate technical architecture and design decisions
3. ✅ Identify potential issues early
4. ✅ Gather user feedback for improvements
5. ✅ Prove feasibility of the solution

### Prototype Scope:
**MVP (Minimum Viable Product)** with essential features that demonstrate value.

---

## 🎯 PROTOTYPE PHASES

### **PHASE 1: Foundation (Week 1-2)** ⭐ PRIORITY
**Status**: Partially Complete ✅

**Components:**
- ✅ Project structure (Frontend + Backend)
- ✅ UI/UX design with UDOM branding
- ⏳ Database setup and schema creation
- ⏳ Basic authentication (login only)
- ⏳ Admin user creation via SQL

**Deliverable:** Users can login and see dashboard

---

### **PHASE 2: Core Features (Week 3-4)** ⭐ CRITICAL
**Status**: Not Started

**Components:**
- ⏳ File upload functionality
- ⏳ File download functionality
- ⏳ File listing and display
- ⏳ Basic file encryption
- ⏳ Storage quota tracking
- ⏳ Admin user registration in UI

**Deliverable:** Users can upload, view, and download files securely

---

### **PHASE 3: Security & Management (Week 5-6)**
**Status**: Not Started

**Components:**
- ⏳ Audit logging for all actions
- ⏳ File sharing between users
- ⏳ User management (Admin panel)
- ⏳ Profile management
- ⏳ Password change

**Deliverable:** Complete user and security management

---

### **PHASE 4: Advanced Features (Week 7-8)**
**Status**: Not Started

**Components:**
- ⏳ Automated backup system
- ⏳ Basic reporting and analytics
- ⏳ File search functionality
- ⏳ Storage usage visualizations

**Deliverable:** Full-featured prototype ready for demo

---

### **PHASE 5: Testing & Refinement (Week 9-10)**
**Status**: Not Started

**Components:**
- ⏳ Unit testing
- ⏳ Integration testing
- ⏳ Security testing
- ⏳ User acceptance testing
- ⏳ Bug fixes and improvements

**Deliverable:** Production-ready system

---

## 🚀 IMPLEMENTATION ROADMAP

### **STEP 1: Database Setup (Day 1)**
**Priority**: HIGH ⭐

**Tasks:**
1. Install and configure PostgreSQL
2. Create database and user
3. Create all required tables:
   ```sql
   - users
   - files (file_metadata)
   - audit_logs
   - shared_files
   - backup_records
   ```
4. Set up initial admin user
5. Test database connectivity

**Time Estimate**: 2-3 hours

**Success Criteria:**
- Database running
- Tables created
- Admin user exists
- Backend connects to database

---

### **STEP 2: Backend Authentication (Day 1-2)**
**Priority**: HIGH ⭐

**Tasks:**
1. Create User entity and repository
2. Implement UserDetailsService
3. Configure Spring Security
4. Implement JWT utilities (generate, validate)
5. Create authentication endpoints:
   - POST `/api/auth/login`
   - GET `/api/auth/me`
6. Test with Postman/Insomnia

**Components to Build:**
```
backend/src/main/java/tz/ac/udom/cloudstorage/
├── config/
│   ├── SecurityConfig.java
│   └── CorsConfig.java
├── security/
│   ├── JwtUtils.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
├── dto/
│   ├── LoginRequest.java
│   └── AuthResponse.java
├── controller/
│   └── AuthController.java
└── service/
    └── AuthService.java
```

**Time Estimate**: 4-6 hours

**Success Criteria:**
- Users can login via API
- JWT tokens generated
- Protected endpoints work

---

### **STEP 3: Frontend Authentication (Day 2)**
**Priority**: HIGH ⭐

**Tasks:**
1. Connect login form to API
2. Handle JWT token storage
3. Implement protected routes
4. Add loading states
5. Handle errors gracefully
6. Test login flow

**Time Estimate**: 2-3 hours

**Success Criteria:**
- Login form works
- Users redirected to dashboard
- Protected routes enforced
- Errors displayed properly

---

### **STEP 4: Admin User Registration (Day 3)**
**Priority**: HIGH ⭐

**Tasks:**
1. Create registration endpoint (admin-protected)
2. Implement password hashing
3. Add user validation
4. Connect frontend modal to API
5. Show success/error feedback
6. Test user creation flow

**Components:**
```
Backend:
- POST /api/auth/register (admin only)
- UserService.createUser()
- Validation and error handling

Frontend:
- Admin Panel modal (already created)
- API integration
- Form validation
```

**Time Estimate**: 3-4 hours

**Success Criteria:**
- Admin can create users
- Passwords hashed with BCrypt
- New users can login
- Validation works

---

### **STEP 5: File Upload Backend (Day 4-5)**
**Priority**: CRITICAL ⭐⭐⭐

**Tasks:**
1. Create FileMetadata entity and repository
2. Configure multipart file upload
3. Implement file storage service:
   - Save file to disk
   - Generate unique filename
   - Calculate checksum
4. Implement basic encryption:
   - AES-256 encryption
   - Secure key management
5. Create file upload endpoint:
   - POST `/api/files/upload`
6. Update user storage quota
7. Test with Postman

**Components:**
```
backend/src/main/java/tz/ac/udom/cloudstorage/
├── service/
│   ├── FileStorageService.java
│   ├── EncryptionService.java
│   └── StorageService.java
├── controller/
│   └── FileController.java
├── dto/
│   ├── FileUploadResponse.java
│   └── FileInfoDTO.java
└── exception/
    ├── StorageException.java
    └── QuotaExceededException.java
```

**Time Estimate**: 6-8 hours

**Success Criteria:**
- Files upload successfully
- Files encrypted on disk
- Metadata saved to database
- Storage quota updated
- Checksums calculated

---

### **STEP 6: File Upload Frontend (Day 5)**
**Priority**: CRITICAL ⭐⭐⭐

**Tasks:**
1. Create file upload component
2. Implement drag-and-drop
3. Add progress bar
4. Show file preview
5. Handle upload errors
6. Refresh file list after upload

**Components:**
```
frontend/src/components/
├── FileUploadModal.jsx
├── FileList.jsx
└── FileItem.jsx
```

**Time Estimate**: 4-5 hours

**Success Criteria:**
- Users can select files
- Upload progress shown
- Success message displayed
- File appears in list

---

### **STEP 7: File Listing & Download (Day 6-7)**
**Priority**: CRITICAL ⭐⭐⭐

**Tasks:**
1. Create file listing endpoint:
   - GET `/api/files`
2. Implement file download:
   - GET `/api/files/{id}/download`
   - Decrypt file
   - Stream to browser
3. Create file delete endpoint:
   - DELETE `/api/files/{id}`
4. Build file manager UI:
   - Display files in cards/table
   - Show file metadata
   - Download button
   - Delete button
5. Add confirmation dialogs

**Time Estimate**: 6-8 hours

**Success Criteria:**
- Files displayed in list
- Files download correctly
- Files can be deleted
- UI responsive and attractive

---

### **STEP 8: Audit Logging (Day 8)**
**Priority**: HIGH ⭐

**Tasks:**
1. Create AuditLog entity and repository
2. Implement AuditService
3. Log all important actions:
   - Login/Logout
   - File upload/download/delete
   - User creation
4. Create audit log endpoint:
   - GET `/api/audit-logs` (admin)
5. Add audit log viewer in admin panel

**Time Estimate**: 4-5 hours

**Success Criteria:**
- All actions logged
- Admin can view logs
- Logs include timestamp, user, IP
- Searchable/filterable

---

### **STEP 9: User Management (Day 9)**
**Priority**: MEDIUM

**Tasks:**
1. Create user management endpoints:
   - GET `/api/users` (admin)
   - GET `/api/users/{id}` (admin)
   - PUT `/api/users/{id}` (admin)
   - DELETE `/api/users/{id}` (admin)
2. Build user list in admin panel
3. Add user edit functionality
4. Add user enable/disable toggle

**Time Estimate**: 4-5 hours

**Success Criteria:**
- Admin can list all users
- Admin can edit user details
- Admin can disable accounts
- Changes reflected immediately

---

### **STEP 10: File Sharing (Day 10-11)**
**Priority**: MEDIUM

**Tasks:**
1. Create SharedFile entity and repository
2. Implement sharing endpoints:
   - POST `/api/files/{id}/share`
   - GET `/api/files/shared-with-me`
   - DELETE `/api/files/{id}/unshare`
3. Add permission checks
4. Build sharing UI:
   - Share modal
   - Shared files view
   - Permission display

**Time Estimate**: 6-7 hours

**Success Criteria:**
- Users can share files
- Shared files accessible
- Permissions enforced
- Can revoke access

---

### **STEP 11: Reporting & Analytics (Day 12)**
**Priority**: LOW

**Tasks:**
1. Create reporting endpoints:
   - GET `/api/reports/storage`
   - GET `/api/reports/activity`
2. Implement data aggregation
3. Build analytics dashboard:
   - Charts and graphs
   - Statistics cards
   - Export functionality

**Time Estimate**: 5-6 hours

**Success Criteria:**
- Storage statistics shown
- Activity trends displayed
- Reports exportable
- Visual representations clear

---

### **STEP 12: Automated Backup (Day 13)**
**Priority**: MEDIUM

**Tasks:**
1. Create backup service
2. Implement database backup:
   - pg_dump command
3. Implement file backup:
   - Copy upload directory
4. Add scheduling with @Scheduled
5. Create backup restoration
6. Test backup/restore process

**Time Estimate**: 4-5 hours

**Success Criteria:**
- Backups run automatically
- Database backed up
- Files backed up
- Can restore from backup

---

### **STEP 13: Testing (Day 14-15)**
**Priority**: HIGH ⭐

**Tasks:**
1. Write unit tests:
   - Service layer tests
   - Repository tests
2. Write integration tests:
   - API endpoint tests
   - Authentication tests
3. Security testing:
   - OWASP ZAP scan
   - Permission tests
4. User acceptance testing:
   - Test with actual users
   - Gather feedback

**Time Estimate**: 8-10 hours

**Success Criteria:**
- 80%+ code coverage
- All critical paths tested
- No major security issues
- Users satisfied

---

### **STEP 14: Documentation & Demo (Day 16)**
**Priority**: HIGH ⭐

**Tasks:**
1. Finalize user manuals
2. Create demo script
3. Prepare presentation
4. Record demo video (optional)
5. Deploy to demo server

**Time Estimate**: 4-6 hours

**Success Criteria:**
- Documentation complete
- Demo script ready
- System deployed
- Presentation prepared

---

## 🛠️ TECHNICAL IMPLEMENTATION PRIORITIES

### **Must Have (Phase 1-2)** ⭐⭐⭐
1. **Authentication**
   - Login system
   - JWT tokens
   - Session management

2. **File Operations**
   - Upload files
   - Download files
   - List files
   - Delete files

3. **Basic Encryption**
   - File encryption at rest
   - Password hashing

4. **User Management**
   - Admin creates users
   - User roles (Admin/Lecturer)

5. **Storage Quota**
   - Track usage
   - Enforce limits

---

### **Should Have (Phase 3)** ⭐⭐
1. **Audit Logging**
   - Log all actions
   - View audit trail

2. **File Sharing**
   - Share with colleagues
   - Permission management

3. **Profile Management**
   - Update profile
   - Change password

4. **Admin Dashboard**
   - User management UI
   - System statistics

---

### **Nice to Have (Phase 4)** ⭐
1. **Automated Backup**
   - Scheduled backups
   - Restore functionality

2. **Reporting**
   - Storage reports
   - Activity reports

3. **Advanced Search**
   - Search files
   - Filter by type/date

4. **File Versioning**
   - Version history
   - Restore previous versions

---

## 🧪 TESTING STRATEGY

### **Unit Testing**
**Tools**: JUnit 5, Mockito

**Test Coverage:**
- Service layer methods
- Repository queries
- Utility functions
- Validation logic

**Target**: 70%+ coverage

---

### **Integration Testing**
**Tools**: Spring Boot Test, RestAssured

**Test Coverage:**
- API endpoints
- Authentication flow
- File upload/download
- Database operations

**Target**: All critical endpoints tested

---

### **Security Testing**
**Tools**: OWASP ZAP, Manual testing

**Test Coverage:**
- Authentication bypass attempts
- Authorization checks
- SQL injection
- XSS vulnerabilities
- File upload security

**Target**: No critical vulnerabilities

---

### **User Acceptance Testing**
**Approach**: Real user testing

**Test Coverage:**
- Login and navigation
- File operations
- User management
- Overall experience

**Target**: Positive user feedback

---

## 📊 DEMO SCENARIOS

### **Scenario 1: Admin Creates User**
1. Admin logs in
2. Navigates to Admin Panel
3. Clicks "Add New User"
4. Fills in lecturer details
5. User created successfully
6. Lecturer can login immediately

---

### **Scenario 2: Lecturer Uploads Exam Paper**
1. Lecturer logs in
2. Sees dashboard with statistics
3. Navigates to File Manager
4. Clicks "Upload File"
5. Selects exam paper (PDF)
6. File uploads with progress
7. File appears encrypted in system
8. Storage quota updated

---

### **Scenario 3: Lecturer Shares Research Document**
1. Lecturer selects document
2. Clicks "Share" button
3. Enters colleague's username
4. Sets view/download permission
5. Colleague notified (or sees in shared files)
6. Colleague can access file
7. All actions logged in audit trail

---

### **Scenario 4: Admin Views System Analytics**
1. Admin logs in
2. Navigates to Admin Panel
3. Views system statistics:
   - Total users
   - Storage usage
   - File count
4. Views audit logs
5. Generates storage report
6. Exports report as PDF

---

### **Scenario 5: System Performs Backup**
1. Scheduled time reached (automated)
2. System backs up database
3. System backs up all files
4. Backup stored securely
5. Admin notified of completion
6. Old backups cleaned up

---

## 🎯 SUCCESS CRITERIA FOR PROTOTYPE

### **Functional Requirements** ✅
- [ ] Users can login securely
- [ ] Admin can create new users
- [ ] Users can upload files (encrypted)
- [ ] Users can download their files
- [ ] Users can delete files
- [ ] Users can share files with colleagues
- [ ] Admin can view all users
- [ ] Admin can view audit logs
- [ ] System tracks storage quota
- [ ] System performs automated backups

### **Non-Functional Requirements** ✅
- [ ] System is secure (encrypted, authenticated)
- [ ] System is responsive (good performance)
- [ ] System is usable (intuitive UI)
- [ ] System is reliable (error handling)
- [ ] System follows UDOM branding

### **Documentation** ✅
- [ ] User manual completed
- [ ] Admin manual completed
- [ ] Technical documentation ready
- [ ] API documentation available
- [ ] Setup guide clear

---

## 📅 REALISTIC TIMELINE

### **Week 1-2: Foundation**
- Day 1-2: Database + Authentication
- Day 3-4: User Registration
- Day 5: Testing & Integration

### **Week 3-4: Core Features**
- Day 6-8: File Upload/Download
- Day 9-10: File Management UI
- Day 11: Audit Logging
- Day 12: Testing

### **Week 5-6: Security & Management**
- Day 13-14: File Sharing
- Day 15-16: User Management
- Day 17: Profile Management
- Day 18: Testing

### **Week 7-8: Advanced Features**
- Day 19-20: Backup System
- Day 21-22: Reporting & Analytics
- Day 23: Search & Filters
- Day 24: Integration Testing

### **Week 9-10: Testing & Refinement**
- Day 25-27: Comprehensive Testing
- Day 28-29: Bug Fixes
- Day 30: Final Demo Preparation

---

## 🚦 NEXT IMMEDIATE STEPS

### **Today (Next 2-3 Hours):**
1. ✅ Set up PostgreSQL database
2. ✅ Create database schema
3. ✅ Create initial admin user
4. ✅ Test database connection

### **Tomorrow:**
1. ⏳ Implement JWT authentication
2. ⏳ Create login API endpoint
3. ⏳ Connect frontend to login API
4. ⏳ Test complete login flow

### **Day After:**
1. ⏳ Implement user registration (admin)
2. ⏳ Connect admin panel to API
3. ⏳ Test user creation flow

---

## 💡 TIPS FOR SUCCESS

1. **Start Simple**: Don't try to build everything at once
2. **Test Frequently**: Test each feature before moving to next
3. **Use Git**: Commit code regularly with clear messages
4. **Document as You Go**: Don't leave documentation for last
5. **Ask for Help**: Don't waste time stuck on one issue
6. **Focus on Core Features**: Get basic version working first
7. **Demo Early**: Show progress to supervisors regularly
8. **Backup Code**: Use GitHub, backup regularly
9. **Work in Iterations**: Build, test, refine, repeat
10. **Stay Organized**: Use todo lists, track progress

---

## 🎓 LEARNING RESOURCES

### **Spring Boot & Security:**
- Spring Boot Documentation
- Baeldung tutorials
- Spring Security JWT examples

### **React & Frontend:**
- React documentation
- Tailwind CSS docs
- Axios documentation

### **Database:**
- PostgreSQL tutorial
- Spring Data JPA guide

### **Security:**
- OWASP guidelines
- Java encryption examples
- JWT best practices

---

**Ready to start building?** Let's begin with Step 1: Database Setup! 🚀

---

**Document**: Prototype Development Plan  
**Version**: 1.0  
**Date**: February 16, 2026  
**Team**: Group 10 - UDOM CSE
