package com.example.schedule.lesson.dto;

import java.time.LocalTime;

import com.example.schedule.lesson.DayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@EndAfterStart
public record LessonRequest(

        @NotBlank
        @Size(max = 100)
        String subject,

        @NotBlank
        @Size(max = 100)
        String teacher,

        @NotBlank
        @Size(max = 50)
        String groupName,

        @NotBlank
        @Size(max = 20)
        String room,

        @NotNull
        DayOfWeek dayOfWeek,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime
) {
}
