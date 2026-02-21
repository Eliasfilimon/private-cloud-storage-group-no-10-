package com.udom.securecloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureCloudStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureCloudStorageApplication.class, args);
        System.out.println("=================================");
        System.out.println("Secure Cloud Storage System Started");
        System.out.println("API: http://localhost:8080");
        System.out.println("=================================");
    }
}
