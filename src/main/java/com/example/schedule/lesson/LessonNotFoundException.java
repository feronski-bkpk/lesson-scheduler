package com.example.schedule.lesson;

public class LessonNotFoundException extends RuntimeException {

    public LessonNotFoundException(Long id) {
        super("Lesson with id " + " not found");
    }
}
