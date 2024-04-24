package com.bank.light.security.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DoubleTypeValidator implements ConstraintValidator<DoubleType, Object> {

    @Override
    public void initialize(DoubleType constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context){
        try {
            Double.parseDouble(obj.toString());
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }
}