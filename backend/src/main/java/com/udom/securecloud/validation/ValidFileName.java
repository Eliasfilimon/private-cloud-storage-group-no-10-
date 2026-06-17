package com.udom.securecloud.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * ValidFileName: Custom validator for file names
 * Checks for path traversal, reserved names, and invalid characters
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileNameValidator.class)
@Documented
public @interface ValidFileName {
    String message() default "Invalid file name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
