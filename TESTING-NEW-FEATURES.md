# Testing Guide for New Features

This guide provides step-by-step instructions for testing all the newly added enhancement features.

## Prerequisites

1. Backend server running on `http://localhost:8080`
2. Frontend server running on `http://localhost:5173`
3. PostgreSQL database running with schema loaded
4. At least one user account created (use default accounts from schema.sql)

### Default Test Accounts
```
Admin Account:
Username: admin
Password: Admin@123

Lecturer Account:
Username: john.doe
Password: Lecturer@123
```

---

## 1. Testing Profile Management

### Test Case 1.1: View Profile
**Steps:**
1. Login with any account
2. Click on your name/avatar in the navbar
3. Select "Profile" from dropdown

**Expected Results:**
- ✅ Profile page loads with current user information
- ✅ Avatar shows user's initials
- ✅ Storage usage progress bar displays correctly
- ✅ Storage percentage is calculated correctly
- ✅ Role badge shows correct role

### Test Case 1.2: Update Profile Information
**Steps:**
1. Navigate to Profile page
2. Modify the following fields:
   - Full Name: "Updated Test User"
   - Email: "updated.test@udom.ac.tz"
   - Department: "Computer Science & Engineering"
3. Click "Save Changes"

**Expected Results:**
- ✅ Success toast notification appears
- ✅ Profile updates immediately
- ✅ Navbar shows updated name
- ✅ Changes persist after page refresh

### Test Case 1.3: Update Profile with Invalid Email
**Steps:**
1. Navigate to Profile page
2. Enter an already existing email
3. Click "Save Changes"

**Expected Results:**
- ✅ Error toast notification appears
- ✅ Error message: "Email already exists"
- ✅ Profile is not updated

### Test Case 1.4: Change Password (Success)
**Steps:**
1. Navigate to Profile page
2. Click "Change" button in password section
3. Enter:
   - Current Password: (your current password)
   - New Password: "NewTest@123"
   - Confirm Password: "NewTest@123"
4. Click "Update Password"

**Expected Results:**
- ✅ Success toast notification appears
- ✅ Password form clears
- ✅ Password form hides
- ✅ Can login with new password after logout

### Test Case 1.5: Change Password (Wrong Current Password)
**Steps:**
1. Navigate to Profile page
2. Click "Change" button
3. Enter incorrect current password
4. Click "Update Password"

**Expected Results:**
- ✅ Error toast notification appears
- ✅ Error message: "Current password is incorrect"
- ✅ Password is not changed

### Test Case 1.6: Change Password (Weak Password)
**Steps:**
1. Navigate to Profile page
2. Click "Change" button
3. Enter weak password like "test123"
4. Click "Update Password"

**Expected Results:**
- ✅ Error message appears
- ✅ Password validation fails
- ✅ Form shows validation hint

### Test Case 1.7: Change Password (Passwords Don't Match)
**Steps:**
1. Navigate to Profile page
2. Click "Change" button
3. New Password: "Test@123"
4. Confirm Password: "Test@456"
5. Click "Update Password"

**Expected Results:**
- ✅ Error toast: "New passwords do not match"
- ✅ Password is not changed

---

## 2. Testing Dashboard Statistics

### Test Case 2.1: View Dashboard (Regular User)
**Steps:**
1. Login as lecturer
2. Navigate to Dashboard (home page)

**Expected Results:**
- ✅ Welcome message with user's name
- ✅ 4 statistic cards displayed:
  - Total Files (shows actual count)
  - Storage Used (shows actual usage with progress)
  - Recent Uploads (last 7 days)
  - Recent Downloads (last 7 days)
- ✅ Storage progress bar shows correct percentage
- ✅ Progress bar color:
  - Green: < 70%
  - Yellow: 70-90%
  - Red: > 90%

### Test Case 2.2: View Dashboard (Admin)
**Steps:**
1. Login as admin
2. Navigate to Dashboard

**Expected Results:**
- ✅ All regular user stats displayed
- ✅ Additional "Active Users" stat card
- ✅ Admin stats show system-wide data
- ✅ All counts are accurate

### Test Case 2.3: Verify Statistics Update
**Steps:**
1. Note current file count on dashboard
2. Upload a new file via File Manager
3. Return to Dashboard

**Expected Results:**
- ✅ Total Files count increases by 1
- ✅ Storage Used increases
- ✅ Recent Uploads increases by 1
- ✅ Storage progress bar updates

### Test Case 2.4: Dashboard with No Files
**Steps:**
1. Login with new user (no files uploaded)
2. View Dashboard

**Expected Results:**
- ✅ Total Files shows "0"
- ✅ Storage Used shows "0 Bytes / 5 GB"
- ✅ Recent Uploads shows "0"
- ✅ Recent Downloads shows "0"
- ✅ Progress bar shows 0%

---

## 3. Testing Enhanced Admin Panel

### Test Case 3.1: View User List
**Steps:**
1. Login as admin
2. Navigate to Admin Panel
3. Scroll to "User Management" section

**Expected Results:**
- ✅ Complete user table displays
- ✅ All users from database shown
- ✅ Each user shows:
  - Avatar with initials
  - Full name
  - Email
  - Role badge (colored)
  - Storage usage
  - Status badge (Active/Inactive)
  - Last login date
- ✅ Total Users stat shows correct count

### Test Case 3.2: User Table Display Accuracy
**Steps:**
1. View admin panel user list
2. Compare with database records

**Expected Results:**
- ✅ All users from database displayed
- ✅ Storage calculations correct
- ✅ Last login dates formatted correctly
- ✅ Role badges show correct colors:
  - ADMIN: Blue
  - LECTURER: Green
- ✅ Status badges correct:
  - Active: Green
  - Inactive: Red

### Test Case 3.3: Create New User and Verify List Update
**Steps:**
1. In Admin Panel, click "Add New User"
2. Fill form:
   - Full Name: "Test User"
   - Username: "test.user"
   - Email: "test.user@udom.ac.tz"
   - Password: "Test@123"
   - Role: "LECTURER"
3. Click "Create User"

**Expected Results:**
- ✅ Success toast appears
- ✅ Modal closes
- ✅ User list refreshes automatically
- ✅ New user appears in table
- ✅ Total Users count increases

### Test Case 3.4: Admin Stats Accuracy
**Steps:**
1. View Admin Panel
2. Note all 4 stat cards

**Expected Results:**
- ✅ Total Users matches user list count
- ✅ All stats show real data (not "0" placeholders)
- ✅ Stats update after creating new user

---

## 4. Testing File Sorting and Filtering

### Test Case 4.1: Sort Files by Name
**Steps:**
1. Login and navigate to File Manager
2. Upload at least 3 files with different names
3. Click "File Name" column header

**Expected Results:**
- ✅ Files sort alphabetically (A-Z)
- ✅ Sort icon changes to up arrow
- ✅ Click again: files sort reverse (Z-A)
- ✅ Sort icon changes to down arrow

### Test Case 4.2: Sort Files by Size
**Steps:**
1. In File Manager with multiple files
2. Click "Size" column header

**Expected Results:**
- ✅ Files sort by size (smallest first)
- ✅ Sort icon updates
- ✅ Click again: largest first
- ✅ Size formatting correct (KB, MB, GB)

### Test Case 4.3: Sort Files by Upload Date
**Steps:**
1. In File Manager with multiple files
2. Click "Upload Date" column header

**Expected Results:**
- ✅ Files sort by date (newest first by default)
- ✅ Sort icon updates
- ✅ Click again: oldest first
- ✅ Dates formatted correctly

### Test Case 4.4: Filter Files by Type
**Steps:**
1. Upload different file types:
   - image.jpg
   - document.pdf
   - video.mp4
   - audio.mp3
   - archive.zip
2. Select each filter option from dropdown

**Expected Results:**
- ✅ "All Files": Shows all files
- ✅ "Images": Shows only .jpg file
- ✅ "Documents": Shows only .pdf file
- ✅ "Videos": Shows only .mp4 file
- ✅ "Audio": Shows only .mp3 file
- ✅ "Archives": Shows only .zip file

### Test Case 4.5: Combined Search and Filter
**Steps:**
1. Have multiple files uploaded
2. Select "Documents" filter
3. Enter search term in search box

**Expected Results:**
- ✅ Shows only documents matching search term
- ✅ Both filters apply simultaneously
- ✅ Empty state shows "Try adjusting your filters" if no matches

### Test Case 4.6: Sort + Filter + Search Combined
**Steps:**
1. Apply filter: "Images"
2. Enter search term
3. Click column header to sort

**Expected Results:**
- ✅ All three operations work together
- ✅ Filtered and searched files sort correctly
- ✅ Sort indicator shows on column
- ✅ Results are accurate

### Test Case 4.7: Empty States
**Steps:**
1. Select a file type filter with no matching files

**Expected Results:**
- ✅ Empty state message appears
- ✅ Message: "Try adjusting your filters"
- ✅ File icon displayed
- ✅ No errors in console

---

## 5. Integration Testing

### Test Case 5.1: Complete User Workflow
**Steps:**
1. Login as admin
2. View Dashboard - note statistics
3. Create new user in Admin Panel
4. Logout and login as new user
5. Update profile information
6. Change password
7. Upload 3 different file types
8. Use sorting and filtering
9. Download a file
10. Delete a file
11. View Dashboard statistics
12. Logout

**Expected Results:**
- ✅ All operations complete successfully
- ✅ Statistics update correctly
- ✅ No errors in console
- ✅ All data persists correctly

### Test Case 5.2: Admin Workflow
**Steps:**
1. Login as admin
2. Check admin dashboard (should have extra stats)
3. View all users in Admin Panel
4. Create 2 new users
5. Verify user list updates
6. Verify statistics update
7. Check user storage usage in list

**Expected Results:**
- ✅ Admin sees system-wide statistics
- ✅ User list shows all users
- ✅ New users appear immediately
- ✅ Statistics are accurate

### Test Case 5.3: Cross-Browser Testing
**Test browsers:**
- Chrome
- Firefox
- Edge
- Safari (if on Mac)

**Expected Results:**
- ✅ All features work in all browsers
- ✅ UI renders correctly
- ✅ No console errors
- ✅ Responsive design works

---

## 6. Performance Testing

### Test Case 6.1: Dashboard Load Time
**Steps:**
1. Clear browser cache
2. Login
3. Measure time to dashboard load with statistics

**Expected Results:**
- ✅ Dashboard loads within 2 seconds
- ✅ Statistics load within 1 second
- ✅ Smooth loading indicators

### Test Case 6.2: File Manager with Many Files
**Steps:**
1. Upload 20+ files
2. Test sorting, filtering, and searching

**Expected Results:**
- ✅ Operations remain fast
- ✅ No UI lag
- ✅ Table renders smoothly

### Test Case 6.3: Profile Updates
**Steps:**
1. Update profile multiple times quickly

**Expected Results:**
- ✅ All updates process correctly
- ✅ No race conditions
- ✅ Loading states prevent duplicate submissions

---

## 7. Error Handling Testing

### Test Case 7.1: Network Error Handling
**Steps:**
1. Stop backend server
2. Try to update profile
3. Try to view dashboard statistics
4. Try to load user list

**Expected Results:**
- ✅ Appropriate error messages show
- ✅ No app crashes
- ✅ User can retry after server restarts

### Test Case 7.2: Invalid Data Handling
**Steps:**
1. Try to update profile with empty fields
2. Try to change password with short password
3. Try to sort empty file list

**Expected Results:**
- ✅ Validation prevents submission
- ✅ Clear error messages
- ✅ No console errors

### Test Case 7.3: Permission Error Handling
**Steps:**
1. Login as regular user
2. Try to access admin-only features

**Expected Results:**
- ✅ Access denied appropriately
- ✅ User redirected or shown error
- ✅ No unauthorized access

---

## 8. Security Testing

### Test Case 8.1: JWT Expiration
**Steps:**
1. Login
2. Wait for token to expire (or manually expire in localStorage)
3. Try to update profile

**Expected Results:**
- ✅ User redirected to login
- ✅ Session ends properly
- ✅ Error message shown

### Test Case 8.2: SQL Injection Prevention
**Steps:**
1. Try to inject SQL in search fields
2. Try in profile update fields

**Expected Results:**
- ✅ No SQL injection possible
- ✅ Input sanitized
- ✅ No database errors

### Test Case 8.3: XSS Prevention
**Steps:**
1. Try to inject scripts in text fields
2. Update profile with script tags

**Expected Results:**
- ✅ Scripts not executed
- ✅ Content sanitized
- ✅ Safe display of user input

---

## Bug Tracking Template

If you find any bugs during testing, document them using this template:

```markdown
## Bug Report

**Bug ID:** [Sequential number]
**Date Found:** [Date]
**Found By:** [Your name]
**Severity:** [Critical/High/Medium/Low]

### Description
[Clear description of the bug]

### Steps to Reproduce
1. [Step 1]
2. [Step 2]
3. [Step 3]

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happens]

### Screenshots
[If applicable]

### Environment
- Browser: [Chrome/Firefox/etc]
- OS: [Windows/Mac/Linux]
- Backend Version: [Version]
- Frontend Version: [Version]

### Console Errors
[Any error messages from browser console]

### Additional Notes
[Any other relevant information]
```

---

## Automated Testing Checklist

### Backend API Testing (Use Postman)
- [ ] GET /api/auth/me (with valid token)
- [ ] PUT /api/profile (with valid data)
- [ ] PUT /api/profile/change-password (with valid data)
- [ ] GET /api/dashboard/stats (as user)
- [ ] GET /api/dashboard/admin/stats (as admin)
- [ ] GET /api/admin/users (as admin)

### Frontend Component Testing
- [ ] Profile page renders
- [ ] Dashboard loads statistics
- [ ] Admin panel shows user list
- [ ] File manager sorting works
- [ ] File manager filtering works
- [ ] All forms validate correctly

---

## Test Results Summary Template

```markdown
# Test Results - [Date]

## Overview
- Total Test Cases: X
- Passed: Y
- Failed: Z
- Pass Rate: (Y/X * 100)%

## Critical Issues Found
1. [Issue 1]
2. [Issue 2]

## Medium Issues Found
1. [Issue 1]
2. [Issue 2]

## Recommendations
- [Recommendation 1]
- [Recommendation 2]

## Sign-off
- Tested By: [Name]
- Date: [Date]
- Status: [PASSED/FAILED/PASSED WITH MINOR ISSUES]
```

---

## Quick Test Script

For rapid testing, run these commands in order:

```bash
# 1. Start backend
cd backend
./mvnw spring-boot:run

# 2. Start frontend (in new terminal)
cd frontend
npm run dev

# 3. Quick functional test
# - Login with admin account
# - Create 1 test user
# - Upload 3 test files
# - Update your profile
# - Change password
# - View dashboard statistics
# - Sort and filter files
# - Verify all features work
```

---

## Success Criteria

All features are considered working if:
- ✅ No critical bugs found
- ✅ All test cases pass
- ✅ Performance meets requirements
- ✅ Security tests pass
- ✅ No console errors
- ✅ Responsive design works
- ✅ Cross-browser compatible
- ✅ Data persists correctly
- ✅ Error handling works properly
- ✅ User experience is smooth

---

**Last Updated:** January 2025  
**Version:** 1.0  
**Status:** Ready for Testing
