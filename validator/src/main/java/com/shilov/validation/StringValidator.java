package com.shilov.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import com.shilov.validation.annotation.StringConstraint;

public class StringValidator implements
        ConstraintValidator<StringConstraint, String> {

    //here we can use @Autowired in Spring Projects

    @Override
    public void initialize(StringConstraint description) {
    }

    @Override
    public boolean isValid(String str,
                           ConstraintValidatorContext cxt) {
        return true;
    }

}
