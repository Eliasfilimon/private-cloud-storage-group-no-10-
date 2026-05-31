package com.udom.securecloud.security;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "Password does not meet complexity requirements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
