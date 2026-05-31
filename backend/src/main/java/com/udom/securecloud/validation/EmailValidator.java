package com.udom.securecloud.validation;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Email validation service for UDOM academic staff
 * Restricts registration to valid UDOM email addresses only
 */
@Component
public class EmailValidator {

    // UDOM email pattern: @udom.ac.tz
    private static final Pattern UDOM_EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@(udom\\.ac\\.tz|students\\.udom\\.ac\\.tz)$", Pattern.CASE_INSENSITIVE);
    
    // General email pattern for basic validation
    private static final Pattern GENERAL_EMAIL_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Validates if the email is a valid UDOM academic email address
     * @param email the email to validate
     * @return true if valid UDOM email, false otherwise
     */
    public boolean isValidUDOMEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return UDOM_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates if the email has a valid format (basic email validation)
     * @param email the email to validate
     * @return true if valid email format, false otherwise
     */
    public boolean isValidEmailFormat(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return GENERAL_EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks if the email belongs to UDOM domain
     * @param email the email to check
     * @return true if UDOM domain, false otherwise
     */
    public boolean isUDOMDomain(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String lowerEmail = email.trim().toLowerCase();
        return lowerEmail.endsWith("@udom.ac.tz") || lowerEmail.endsWith("@students.udom.ac.tz");
    }

    /**
     * Gets the domain from an email address
     * @param email the email address
     * @return the domain part of the email
     */
    public String getDomain(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }
        int atIndex = email.trim().lastIndexOf('@');
        return atIndex > 0 ? email.trim().substring(atIndex + 1) : "";
    }

    /**
     * Normalizes email address (converts to lowercase and trims)
     * @param email the email to normalize
     * @return normalized email
     */
    public String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validates email for UDOM academic staff registration
     * This is the main validation method for user registration
     * @param email the email to validate
     * @return validation result with message
     */
    public EmailValidationResult validateForRegistration(String email) {
        if (email == null || email.trim().isEmpty()) {
            return EmailValidationResult.invalid("Email address is required");
        }

        String normalizedEmail = normalizeEmail(email);

        if (!isValidEmailFormat(normalizedEmail)) {
            return EmailValidationResult.invalid("Invalid email format");
        }

        if (!isUDOMDomain(normalizedEmail)) {
            return EmailValidationResult.invalid("Only UDOM email addresses are allowed (@udom.ac.tz or @students.udom.ac.tz)");
        }

        if (!isValidUDOMEmail(normalizedEmail)) {
            return EmailValidationResult.invalid("Email format must be: username@udom.ac.tz or username@students.udom.ac.tz");
        }

        return EmailValidationResult.valid("Valid UDOM email address");
    }

    /**
     * Email validation result
     */
    public static class EmailValidationResult {
        private final boolean valid;
        private final String message;

        private EmailValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static EmailValidationResult valid(String message) {
            return new EmailValidationResult(true, message);
        }

        public static EmailValidationResult invalid(String message) {
            return new EmailValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
