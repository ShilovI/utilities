package com.shilov.validation.annotation;

import com.shilov.validation.StringValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StringValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringConstraint {
    String message() default "Invalid string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}