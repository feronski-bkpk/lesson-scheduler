package com.example.schedule.lesson.dto;

import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndAfterStartValidator.class)
public @interface EndAfterStart {

    String message() default "endTime must be strictly after startTime";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
