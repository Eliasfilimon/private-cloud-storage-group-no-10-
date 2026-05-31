package com.udom.securecloud.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${password.min-length:8}")
    private int minLength;

    @Value("${password.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${password.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${password.require-digits:true}")
    private boolean requireDigits;

    @Value("${password.require-special:true}")
    private boolean requireSpecial;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < minLength) {
            return false;
        }

        if (requireUppercase && !UPPERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireLowercase && !LOWERCASE_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireDigits && !DIGIT_PATTERN.matcher(password).find()) {
            return false;
        }

        if (requireSpecial && !SPECIAL_PATTERN.matcher(password).find()) {
            return false;
        }

        if (password.contains(" ")) {
            return false;
        }

        return true;
    }
}
