package vn.vcraft.backend.validation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Utf8BytesValidator.class)
public @interface Utf8Bytes {
    String message() default "Không được vượt quá {max} byte UTF-8.";
    int max();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
