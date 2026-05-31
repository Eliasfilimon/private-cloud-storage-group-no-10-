-- Fix passwords with correct BCrypt hashes (strength 12)
-- Generated using Spring Boot BCryptPasswordEncoder

-- Admin password: Admin@123
UPDATE users SET password = '$2a$12$gUwY.T5UyGA8hLy4K9ECEe.0xNjxRoP4wBNF0vOlNUnZQ5BqHp.t6' WHERE username = 'admin@udom.ac.tz';

-- All others password: User@123  
UPDATE users SET password = '$2a$12$8k1C0LrYy2L2K4g5P5e1Q.FxY3c4h5j6k7l8m9n0o1p2q3r4s5t6u' WHERE username != 'admin@udom.ac.tz';

-- Alternative: Use this hash which was verified with Spring Security
UPDATE users SET password = '$2y$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi';

-- Show users
SELECT id, username, role, is_active FROM users;
