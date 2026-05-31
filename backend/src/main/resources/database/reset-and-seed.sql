-- Reset Database and Seed with Email-based Login
-- Run this in PostgreSQL to reset all data and create new users

-- Step 1: Truncate all tables (delete all data)
TRUNCATE TABLE shared_files CASCADE;
TRUNCATE TABLE backup_records CASCADE;
TRUNCATE TABLE audit_logs CASCADE;
TRUNCATE TABLE file_metadata CASCADE;
TRUNCATE TABLE folders CASCADE;
TRUNCATE TABLE users CASCADE;

-- Step 2: Reset sequences
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE folders_id_seq RESTART WITH 1;
ALTER SEQUENCE file_metadata_id_seq RESTART WITH 1;
ALTER SEQUENCE audit_logs_id_seq RESTART WITH 1;
ALTER SEQUENCE shared_files_id_seq RESTART WITH 1;
ALTER SEQUENCE backup_records_id_seq RESTART WITH 1;

-- Step 3: Insert new admin user (email as username)
-- Password: Admin@123 (BCrypt hashed)
INSERT INTO users (id, username, email, password, full_name, role, department, storage_quota, is_active, created_at, updated_at)
VALUES (
    1,
    'admin@udom.ac.tz',  -- email is now the username
    'admin@udom.ac.tz',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIRjYz7iei',
    'System Administrator',
    'ADMIN',
    'IT Department',
    10737418240, -- 10GB
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Step 4: Insert sample lecturer users
-- Password for all: User@123

-- Lecturer 1
INSERT INTO users (id, username, email, password, full_name, role, department, storage_quota, is_active, created_at, updated_at)
VALUES (
    2,
    'john.doe@udom.ac.tz',
    'john.doe@udom.ac.tz',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Dr. John Doe',
    'LECTURER',
    'Computer Science',
    5368709120, -- 5GB
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Lecturer 2
INSERT INTO users (id, username, email, password, full_name, role, department, storage_quota, is_active, created_at, updated_at)
VALUES (
    3,
    'jane.smith@udom.ac.tz',
    'jane.smith@udom.ac.tz',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Dr. Jane Smith',
    'LECTURER',
    'Mathematics',
    5368709120,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Lecturer 3
INSERT INTO users (id, username, email, password, full_name, role, department, storage_quota, is_active, created_at, updated_at)
VALUES (
    4,
    'michael.johnson@udom.ac.tz',
    'michael.johnson@udom.ac.tz',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Prof. Michael Johnson',
    'LECTURER',
    'Physics',
    5368709120,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Staff user
INSERT INTO users (id, username, email, password, full_name, role, department, storage_quota, is_active, created_at, updated_at)
VALUES (
    5,
    'sarah.wilson@udom.ac.tz',
    'sarah.wilson@udom.ac.tz',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Sarah Wilson',
    'STAFF',
    'Administration',
    5368709120,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Verify the users
SELECT id, username, email, full_name, role, department, is_active FROM users;
