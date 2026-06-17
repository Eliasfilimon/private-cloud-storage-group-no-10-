package com.udom.securecloud.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * ValidFolderName: Custom validator for folder names
 * Checks for path traversal, reserved names, and invalid characters
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FolderNameValidator.class)
@Documented
public @interface ValidFolderName {
    String message() default "Invalid folder name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
