package com.example.schedule.lesson.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EndAfterStartValidator implements ConstraintValidator<EndAfterStart, LessonRequest> {

    @Override
    public boolean isValid(LessonRequest value, ConstraintValidatorContext context) {
        if (value == null || value.startTime() == null || value.endTime() == null) {
            return true;
        }
        return value.endTime().isAfter(value.startTime());
    }
}
