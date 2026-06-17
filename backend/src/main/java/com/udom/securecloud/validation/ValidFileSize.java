package com.udom.securecloud.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * ValidFileSize: Custom validator for file sizes
 * Validates that file size doesn't exceed maximum allowed
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
@Documented
public @interface ValidFileSize {
    long max() default 500; // MB
    String message() default "File size exceeds maximum allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
