package com.grosso.chat.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ValidPassword {
    String message() default "Password can't be null!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
