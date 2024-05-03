package com.bank.light.security.validators;

import com.bank.light.models.UserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        try {
            final UserDto user = (UserDto) obj;
            return user.getPassword().equals(user.getMatchingPassword());
        } catch (NullPointerException e) {
            return false;
        }
    }
}