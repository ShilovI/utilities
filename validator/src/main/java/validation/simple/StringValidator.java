package validation.simple;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import validation.simple.annotation.StringConstraint;

public class StringValidator implements
        ConstraintValidator<StringConstraint, String> {

    //here we can use @Autowired because it is Spring Component

    @Override
    public void initialize(StringConstraint description) {
    }

    @Override
    public boolean isValid(String str,
                           ConstraintValidatorContext cxt) {
        return true;
    }

}
