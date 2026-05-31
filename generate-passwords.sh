#!/bin/bash
# Generate password hashes using the running backend container

echo "Testing password hash generation..."

# Create a temporary Java file
cat > /tmp/TestHash.java << 'EOF'
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        // Generate hash for Admin@123
        String adminHash = encoder.encode("Admin@123");
        System.out.println("Admin@123 hash: " + adminHash);
        System.out.println("Verify Admin@123: " + encoder.matches("Admin@123", adminHash));
        
        // Generate hash for User@123
        String userHash = encoder.encode("User@123");
        System.out.println("User@123 hash: " + userHash);
        System.out.println("Verify User@123: " + encoder.matches("User@123", userHash));
        
        // Also test 'password'
        String simpleHash = encoder.encode("password");
        System.out.println("password hash: " + simpleHash);
        System.out.println("Verify password: " + encoder.matches("password", simpleHash));
    }
}
EOF

echo "Java file created at /tmp/TestHash.java"
echo "Run this manually inside the backend container or compile locally with Spring Security"
