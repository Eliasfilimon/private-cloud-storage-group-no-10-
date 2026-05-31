package com.udom.securecloud.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        
        String[] passwords = {"Admin@123", "User@123"};
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println("Verify: " + encoder.matches(password, hash));
            System.out.println("---");
        }
    }
}
