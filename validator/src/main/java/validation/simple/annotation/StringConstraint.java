package validation.simple.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import validation.simple.StringValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StringValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringConstraint {
    String message() default "Invalid description";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}