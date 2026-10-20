package com.example.schedule.lesson.dto;

import com.example.schedule.lesson.DayOfWeek;
import com.example.schedule.lesson.Lesson;

import java.time.LocalTime;

public record LessonResponse(
        Long id,
        String subject,
        String teacher,
        String groupName,
        String room,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
            lesson.getId(),
            lesson.getSubject(),
            lesson.getTeacher(),
            lesson.getGroupName(),
            lesson.getRoom(),
            lesson.getDayOfWeek(),
            lesson.getStartTime(),
            lesson.getEndTime()
        );
    }
}
