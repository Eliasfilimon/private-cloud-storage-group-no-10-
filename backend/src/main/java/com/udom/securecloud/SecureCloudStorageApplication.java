package com.udom.securecloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SecureCloudStorageApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SecureCloudStorageApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SecureCloudStorageApplication.class);
        app.run(args);
        System.out.println("=================================");
        System.out.println("Secure Cloud Storage System Started");
        System.out.println("API: http://localhost:8083");
        System.out.println("Health Check: http://localhost:8083/api/health");
        System.out.println("=================================");
    }
}
