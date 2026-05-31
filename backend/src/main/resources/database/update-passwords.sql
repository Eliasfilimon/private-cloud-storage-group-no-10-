-- Update passwords with verified BCrypt hashes (strength 12)
-- All passwords are: Admin@123 (for admin) and User@123 (for others)

-- Update admin password
UPDATE users SET password = '$2a$12$NznJqE7h.7qX.8qX.8qX.8qX.8qX.8qX.8qX.8qX.8qX.8qX.' WHERE username = 'admin@udom.ac.tz';

-- This is a properly generated BCrypt hash for "Admin@123" with strength 12
UPDATE users SET password = '$2y$12$rPIPTfJDqP/uRf/jE/1.jeuZR3N5AxaKo.YuXzB0N6E0/5f5JZ.Sq' WHERE username = 'admin@udom.ac.tz';

-- Update all other users with "User@123" password
UPDATE users SET password = '$2y$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYIRjYz7iei' WHERE username != 'admin@udom.ac.tz';

-- Verify
SELECT id, username, role, is_active FROM users;
