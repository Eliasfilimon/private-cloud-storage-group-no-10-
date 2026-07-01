
//hiini kwaa ajili ya kutuma emails kw user once account inapo kua created but kwa sasa haijawa activated
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

    /**
     * Fix (Issue 4): If SMTP_USERNAME is not set in env, username is blank.
     * We guard every send call so the app never crashes due to missing SMTP config.
     * Emails are silently skipped when SMTP is unconfigured — non-critical operations.
     */
    @Value("${spring.mail.username:}")
    private String smtpUsername;

    /** Returns true only when SMTP credentials are actually configured. */
    private boolean isSmtpConfigured() {
        return smtpUsername != null && !smtpUsername.isBlank();
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        if (!isSmtpConfigured()) {
            log.warn("SMTP not configured (SMTP_USERNAME is empty). Password reset email NOT sent to {}. " +
                     "Set SMTP_USERNAME and SMTP_PASSWORD environment variables to enable email.", toEmail);
            return;
        }
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - UDOM Secure Cloud Storage");
            message.setText(String.format(
                "Hello,\n\n" +
                "You have requested to reset your password for the UDOM Secure Cloud Storage system.\n\n" +
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
        if (!isSmtpConfigured()) {
            log.warn("SMTP not configured. Welcome email NOT sent to {}. " +
                     "Temp password for this user is: {}", toEmail, tempPassword);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to UDOM Secure Cloud Storage - Account Created");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your UDOM Secure Cloud Storage account has been created.\n\n" +
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
        if (!isSmtpConfigured()) {
            log.warn("SMTP not configured. File-shared notification NOT sent to {}.", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("New File Shared With You - UDOM Secure Cloud Storage");
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
