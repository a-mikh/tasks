package com.anton.tasks.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxBytesValidator.class)
@Documented
public @interface MaxBytes {
    int value();
    String message() default "The limit of bytes is exceeded";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
