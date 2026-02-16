package com.grosso.chat.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchingValidator implements ConstraintValidator<PasswordMatching, Object> {

    private String password;
    private String repeatedPassword;
    private String message;

    @Override
    public void initialize(PasswordMatching constraintAnnotation) {
        this.password = constraintAnnotation.password();
        this.repeatedPassword = constraintAnnotation.repeatedPassword();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object passwordValue = wrapper.getPropertyValue(password);
        Object repeatedPasswordValue = wrapper.getPropertyValue(repeatedPassword);

        String p = passwordValue == null ? null : passwordValue.toString();
        String rp = repeatedPasswordValue == null ? null : repeatedPasswordValue.toString();

        boolean isValid = p != null && p.equals(rp);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(repeatedPassword)
                    .addConstraintViolation();
        }

        return isValid;
    }
}