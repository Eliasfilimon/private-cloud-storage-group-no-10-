package com.udom.securecloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:3002}")
    private String frontendUrl;

    @Value("${spring.mail.from:noreply@udom.ac.tz}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - Secure Cloud Storage");
            message.setText(String.format(
                "Hello,\n\n" +
                "You have requested to reset your password for the Secure Cloud Storage system.\n\n" +
                "Click the link below to reset your password:\n%s\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "UDOM IT Support",
                resetLink
            ));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName, String username, String tempPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Secure Cloud Storage - Account Created");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your Secure Cloud Storage account has been created.\n\n" +
                "Username: %s\n" +
                "Temporary Password: %s\n\n" +
                "Please log in at: %s\n\n" +
                "You will be required to change your password on first login.\n\n" +
                "Best regards,\n" +
                "UDOM IT Support",
                fullName, username, tempPassword, frontendUrl
            ));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendFileSharedEmail(String toEmail, String sharerName, String fileName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("New File Shared With You - Secure Cloud Storage");
            message.setText(String.format(
                "Hello,\n\n" +
                "%s has shared a file with you: %s\n\n" +
                "Log in to your account to view the shared file:\n%s\n\n" +
                "Best regards,\n" +
                "UDOM IT Support",
                sharerName, fileName, frontendUrl
            ));

            mailSender.send(message);
            log.info("File shared email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send file shared email to {}: {}", toEmail, e.getMessage());
        }
    }
}
