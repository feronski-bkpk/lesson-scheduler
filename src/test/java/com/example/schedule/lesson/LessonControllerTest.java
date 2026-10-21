package com.example.schedule.lesson;

import com.example.schedule.common.GlobalExceptionHandler;
import com.example.schedule.lesson.dto.LessonRequest;
import com.example.schedule.lesson.dto.LessonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LessonController.class)
@Import(GlobalExceptionHandler.class)
class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService service;

    private LessonRequest validRequest() {
        return new LessonRequest(
                "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Test
    void create_returns201_whenValid() throws Exception {
        LessonResponse response = new LessonResponse(
                1L, "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(service.create(any(LessonRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subject").value("Math"))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"));
    }

    @Test
    void create_returns400_whenBodyInvalid() throws Exception {
        LessonRequest invalid = new LessonRequest(
                "", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("subject")));
    }

    @Test
    void create_returns400_whenEndBeforeStart() throws Exception {
        LessonRequest invalid = new LessonRequest(
                "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(10, 0));

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(org.hamcrest.Matchers.containsString("endTime must be strictly after startTime")));
    }

    @Test
    void create_returns409_whenConflict() throws Exception {
        when(service.create(any(LessonRequest.class)))
                .thenThrow(new LessonConflictException("Conflict with existing lesson id=2"));

        mockMvc.perform(post("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void get_returns200_whenFound() throws Exception {
        LessonResponse response = new LessonResponse(
                5L, "Physics", "Petrov", "G-102", "B-2",
                DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(13, 0));
        when(service.get(5L)).thenReturn(response);

        mockMvc.perform(get("/api/lessons/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.teacher").value("Petrov"));
    }

    @Test
    void get_returns404_whenNotFound() throws Exception {
        when(service.get(99L)).thenThrow(new LessonNotFoundException(99L));

        mockMvc.perform(get("/api/lessons/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void get_returns400_whenIdNotANumber() throws Exception {
        mockMvc.perform(get("/api/lessons/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void findAll_passesFiltersToService() throws Exception {
        when(service.findAll(eq("G-101"), eq("Ivanov"), eq(DayOfWeek.MONDAY)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/lessons")
                        .param("groupName", "G-101")
                        .param("teacher", "Ivanov")
                        .param("dayOfWeek", "MONDAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void findAll_returns400_whenDayOfWeekInvalid() throws Exception {
        mockMvc.perform(get("/api/lessons").param("dayOfWeek", "HOLIDAY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void update_returns200_whenValid() throws Exception {
        LessonResponse response = new LessonResponse(
                7L, "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(service.update(eq(7L), any(LessonRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/lessons/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void delete_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/lessons/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_returns404_whenMissing() throws Exception {
        doThrow(new LessonNotFoundException(3L)).when(service).delete(3L);

        mockMvc.perform(delete("/api/lessons/3"))
                .andExpect(status().isNotFound());
    }
}
