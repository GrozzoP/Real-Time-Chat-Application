package com.grosso.chat.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
@Constraint(validatedBy = PasswordMatchingValidator.class)
public @interface PasswordMatching {
    String message() default "Password must match!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String password();

    String repeatedPassword();

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        PasswordMatching[] value();
    }
}
