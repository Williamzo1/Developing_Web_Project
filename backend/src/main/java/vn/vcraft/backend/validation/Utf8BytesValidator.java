package vn.vcraft.backend.validation;

import java.nio.charset.StandardCharsets;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Utf8BytesValidator implements ConstraintValidator<Utf8Bytes, String> {
    private int max;

    @Override
    public void initialize(Utf8Bytes annotation) {
        max = annotation.max();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
