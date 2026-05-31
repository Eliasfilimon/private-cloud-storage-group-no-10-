package com.udom.securecloud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FileProperties: Configuration for file upload and storage settings
 * 
 * Maps properties from:
 * file.upload-dir=<path>
 * file.backup.location=<path>
 * file.upload.max-size=<bytes>
 * file.upload.allowed-extensions=<csv>
 */
@Component
@ConfigurationProperties(prefix = "file")
@Data
public class FileProperties {
    /**
     * Root directory for file uploads
     * Default: ./uploads
     */
    private String uploadDir = "./uploads";
    
    /**
     * Backup configuration
     */
    private Backup backup = new Backup();
    
    /**
     * Upload configuration
     */
    private Upload upload = new Upload();
    
    /**
     * Backup settings
     */
    @Data
    public static class Backup {
        /**
         * Directory for backup files
         * Default: ./backups
         */
        private String location = "./backups";
    }
    
    /**
     * File upload settings
     */
    @Data
    public static class Upload {
        /**
         * Maximum file size in bytes
         * Default: 104857600 (100 MB)
         */
        private long maxSize = 104857600L;
        
        /**
         * Allowed file extensions (comma-separated)
         * Default: pdf,docx,doc,xlsx,xls,pptx,ppt,txt,csv,jpg,jpeg,png,gif,webp,zip,rar,7z,mp4,avi,mov
         */
        private String allowedExtensions = "pdf,docx,doc,xlsx,xls,pptx,ppt,txt,csv,jpg,jpeg,png,gif,webp,zip,rar,7z,mp4,avi,mov";
    }
}
