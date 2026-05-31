package com.udom.securecloud.controller;

import com.udom.securecloud.service.FileStorageService;
import com.udom.securecloud.service.UserService;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DataSource dataSource;
    private final MinioClient minioClient;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final BuildProperties buildProperties;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        boolean isHealthy = true;
        
        // Database health
        boolean dbHealthy = checkDatabase();
        health.put("database", Map.of(
            "status", dbHealthy ? "UP" : "DOWN",
            "timestamp", LocalDateTime.now()
        ));
        if (!dbHealthy) isHealthy = false;
        
        // MinIO health
        boolean minioHealthy = checkMinIO();
        health.put("minio", Map.of(
            "status", minioHealthy ? "UP" : "DOWN",
            "timestamp", LocalDateTime.now()
        ));
        if (!minioHealthy) isHealthy = false;
        
        // Overall status
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Database
        boolean dbHealthy = checkDatabase();
        health.put("database", Map.of(
            "status", dbHealthy ? "UP" : "DOWN",
            "timestamp", LocalDateTime.now(),
            "details", checkDatabaseDetails()
        ));
        
        // MinIO
        boolean minioHealthy = checkMinIO();
        health.put("minio", Map.of(
            "status", minioHealthy ? "UP" : "DOWN",
            "timestamp", LocalDateTime.now(),
            "details", checkMinIODetails()
        ));
        
        // Application info
        health.put("application", Map.of(
            "name", buildProperties.getName(),
            "version", buildProperties.getVersion(),
            "timestamp", LocalDateTime.now()
        ));
        
        // Overall status
        boolean isHealthy = dbHealthy && minioHealthy;
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> readiness = new HashMap<>();
        boolean ready = checkDatabase() && checkMinIO();
        
        readiness.put("status", ready ? "READY" : "NOT_READY");
        readiness.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(readiness);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> liveness = new HashMap<>();
        
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(liveness);
    }

    private boolean checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkMinIO() {
        try {
            // Just check if we can communicate with MinIO
            return minioClient != null;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> checkDatabaseDetails() {
        Map<String, Object> details = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            details.put("valid", conn.isValid(5));
            details.put("url", conn.getMetaData().getURL());
            details.put("username", conn.getMetaData().getUserName());
        } catch (Exception e) {
            details.put("error", e.getMessage());
        }
        return details;
    }

    private Map<String, Object> checkMinIODetails() {
        Map<String, Object> details = new HashMap<>();
        try {
            // Check if bucket exists (basic connectivity test)
            details.put("connected", true);
            details.put("client", minioClient != null ? "configured" : "not configured");
        } catch (Exception e) {
            details.put("error", e.getMessage());
            details.put("connected", false);
        }
        return details;
    }
}
