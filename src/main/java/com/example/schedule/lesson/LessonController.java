package com.example.schedule.lesson;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.schedule.lesson.dto.LessonRequest;
import com.example.schedule.lesson.dto.LessonResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse create(@Valid @RequestBody LessonRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public LessonResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<LessonResponse> findAll(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) String teacher,
            @RequestParam(required = false) DayOfWeek dayOfWeek) {
                return service.findAll(groupName, teacher, dayOfWeek);
    }

    @PutMapping("/{id}")
    public LessonResponse update(@PathVariable Long id, @Valid @RequestBody LessonRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
