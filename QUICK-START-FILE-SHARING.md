# 🚀 Quick Start Guide - File Sharing Feature

## Step 1: Update Database Schema

Run this SQL script to add the shared_files table:

```bash
cd backend
psql -U postgres -d secure_cloud_storage -f src/main/resources/database/schema.sql
```

Or manually execute:

```sql
CREATE TABLE IF NOT EXISTS shared_files (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL REFERENCES file_metadata(id) ON DELETE CASCADE,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    owner_username VARCHAR(50) NOT NULL,
    shared_with_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shared_with_username VARCHAR(50) NOT NULL,
    permission VARCHAR(20) NOT NULL CHECK (permission IN ('VIEW', 'DOWNLOAD', 'EDIT')) DEFAULT 'VIEW',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    last_accessed_at TIMESTAMP,
    UNIQUE(file_id, shared_with_id)
);

CREATE INDEX IF NOT EXISTS idx_shared_files_owner ON shared_files(owner_id);
CREATE INDEX IF NOT EXISTS idx_shared_files_shared_with ON shared_files(shared_with_id);
```

## Step 2: Start Backend

```bash
cd backend
./mvnw spring-boot:run
```

Wait for: `Started SecureCloudStorageApplication in X seconds`

## Step 3: Start Frontend

```bash
cd frontend
npm install  # Only needed first time or after package.json changes
npm run dev
```

Visit: `http://localhost:5173`

## Step 4: Create Test Users (if needed)

1. Login as **admin** (password: `Admin@123`)
2. Go to **Admin** panel
3. Click **Create New User**
4. Create at least 2 lecturer accounts:

**User 1:**
- Username: `lecturer2`
- Email: `lecturer2@udom.ac.tz`
- Full Name: `Dr. Jane Smith`
- Role: `LECTURER`
- Department: `Biology`
- Password: `Lecturer@123`

**User 2:**
- Username: `lecturer3`
- Email: `lecturer3@udom.ac.tz`
- Full Name: `Dr. Robert Johnson`
- Role: `LECTURER`
- Department: `Physics`
- Password: `Lecturer@123`

## Step 5: Test File Sharing

### 5.1 Upload and Share a File

1. **Logout** from admin
2. **Login** as `lecturer1` (password: `Lecturer@123`)
3. Go to **Files** page
4. Click **Upload File**
5. Select any document (PDF, Word, etc.)
6. Enable **Encrypt file (AES-256)**
7. Click **Upload**
8. Find your uploaded file
9. Click the **blue Share icon** (📤)
10. In the Share modal:
    - Click on `Dr. Jane Smith` to select
    - Permission: **View & Download**
    - Expires In: `7` days
11. Click **Share File**
12. ✅ See success message

### 5.2 View Files You've Shared

1. Click **Shared** in navigation
2. Click **Shared By Me** tab
3. ✅ See your shared file with recipient info
4. ✅ Note the permission and expiration

### 5.3 Access Shared File (as Recipient)

1. **Logout** from lecturer1
2. **Login** as `lecturer2` (password: `Lecturer@123`)
3. Click **Shared** in navigation
4. **Shared With Me** tab should be active
5. ✅ See the file shared with you
6. ✅ See the owner name (Dr. John Doe)
7. Click **Download** button
8. ✅ File downloads successfully

### 5.4 Revoke Access

1. **Logout** and **Login** back as `lecturer1`
2. Go to **Shared** → **Shared By Me**
3. Find the shared file
4. Click **Unshare** button
5. Confirm
6. ✅ File disappears from the list

### 5.5 Verify Revoked Access

1. **Logout** and **Login** as `lecturer2`
2. Go to **Shared** → **Shared With Me**
3. ✅ Shared file is gone

## Step 6: Test Multiple User Sharing

1. Login as `lecturer1`
2. Upload a new file
3. Click Share
4. Select **both** `Dr. Jane Smith` AND `Dr. Robert Johnson`
5. Click Share File
6. ✅ See "File shared with 2 user(s)"
7. Go to **Shared** → **Shared By Me**
8. ✅ See 2 entries (one for each recipient)

## 🎯 What You Should See

### File Manager Page
- Upload button (top right)
- Search bar
- Filter dropdown (All Files, Images, Documents, etc.)
- File table with columns: Name, Size, Upload Date, Encrypted, Actions
- **Share button** (blue icon) in Actions column
- Download button (green icon)
- Delete button (red icon)

### Share Modal
- File name at top
- Permission dropdown (View Only / View & Download)
- Expiration field (optional)
- List of all users with:
  - Avatar with initials
  - Full name
  - Email
  - Department
- Selected users highlighted in blue with checkmark
- Cancel and Share File buttons

### Shared Files Page
- Two tabs: "Shared With Me" and "Shared By Me"
- Table showing:
  - File icon and name
  - Owner/Recipient name
  - Permission badge
  - When shared ("2 hours ago")
  - Expiration status
  - Actions (Download/Unshare)

### Navbar
- Home icon: **Dashboard**
- Folder icon: **Files**
- Users icon: **Shared** ← NEW!
- User icon: **Profile**
- Cog icon: **Admin** (admin only)

## 🐛 Common Issues

### Issue: "Failed to load users" in Share modal
**Fix**: Ensure you have other active users. Login as admin and create more users.

### Issue: Share button doesn't work
**Fix**: 
1. Check browser console for errors
2. Verify backend is running
3. Check database has shared_files table

### Issue: Downloaded file is corrupted
**Fix**: This is a known issue with encrypted files - the decryption happens on the server. File should be correct.

### Issue: Can't see shared files
**Fix**:
1. Check you're on the correct tab (With Me vs By Me)
2. Verify share wasn't revoked
3. Check expiration hasn't passed

## ✅ Success Criteria

You've successfully implemented file sharing when:

- ✅ Share button appears on all your files
- ✅ Share modal opens and shows other users
- ✅ You can select users and set permissions
- ✅ Files appear in "Shared By Me" after sharing
- ✅ Recipients can see files in "Shared With Me"
- ✅ Recipients can download files (if permission allows)
- ✅ Unshare removes access immediately
- ✅ Audit logs show SHARE_FILE and UNSHARE_FILE actions

## 📊 Check Audit Logs

To verify everything is being tracked:

```sql
SELECT * FROM audit_logs 
WHERE action IN ('SHARE_FILE', 'UNSHARE_FILE') 
ORDER BY created_at DESC 
LIMIT 10;
```

You should see entries with:
- User who performed the action
- Timestamp
- IP address
- Success status
- Details about who file was shared with

## 🎉 Congratulations!

Your secure cloud storage now has:
- ✅ Complete file upload/download
- ✅ File encryption (AES-256)
- ✅ User authentication & authorization
- ✅ Admin user management
- ✅ Dashboard with statistics
- ✅ Profile management
- ✅ **FILE SHARING** ← NEW!
- ✅ Audit logging
- ✅ Search and filtering

You've built a **production-ready Dropbox/Google Drive alternative** for academic institutions! 🎓

## Next: Show Your Supervisor

Demonstrate these features:
1. **Security**: Show encrypted storage, JWT auth, audit logs
2. **Usability**: Upload, download, share files easily
3. **Collaboration**: Share files with permissions
4. **Administration**: User management, system stats
5. **Access Control**: Staff-only, role-based permissions

Good luck! 🚀
