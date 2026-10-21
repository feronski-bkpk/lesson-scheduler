package com.example.schedule.lesson;

import com.example.schedule.lesson.dto.LessonRequest;
import com.example.schedule.lesson.dto.LessonResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository repository;

    @InjectMocks
    private LessonService service;

    private LessonRequest request;

    @BeforeEach
    void setUp() {
        request = new LessonRequest(
                "Math",
                "Ivanov",
                "G-101",
                "A-1",
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
    }

    @Test
    void create_savesLesson_whenNoConflicts() {
        when(repository.findConflicts(
                eq(DayOfWeek.MONDAY), eq(LocalTime.of(10, 0)), eq(LocalTime.of(11, 0)),
                eq("G-101"), eq("Ivanov"), eq("A-1"), eq(-1L)))
                .thenReturn(List.of());
        when(repository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson toSave = invocation.getArgument(0);
            return new Lesson(
                    toSave.getSubject(), toSave.getTeacher(), toSave.getGroupName(),
                    toSave.getRoom(), toSave.getDayOfWeek(),
                    toSave.getStartTime(), toSave.getEndTime());
        });

        LessonResponse response = service.create(request);

        assertThat(response.subject()).isEqualTo("Math");
        assertThat(response.teacher()).isEqualTo("Ivanov");
        assertThat(response.groupName()).isEqualTo("G-101");
        assertThat(response.room()).isEqualTo("A-1");
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(response.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(11, 0));
        verify(repository).save(any(Lesson.class));
    }

    @Test
    void create_throwsConflict_whenRepositoryFindsOverlap() {
        Lesson conflicting = new Lesson(
                "Physics", "Petrov", "G-101", "B-2",
                DayOfWeek.MONDAY, LocalTime.of(10, 30), LocalTime.of(11, 30));
        when(repository.findConflicts(
                eq(DayOfWeek.MONDAY), eq(LocalTime.of(10, 0)), eq(LocalTime.of(11, 0)),
                eq("G-101"), eq("Ivanov"), eq("A-1"), eq(-1L)))
                .thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(LessonConflictException.class)
                .hasMessageContaining("G-101");

        verify(repository, never()).save(any(Lesson.class));
    }

    @Test
    void create_passesMinusOneAsExcludeId_toNotExcludeAnything() {
        when(repository.findConflicts(any(), any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(List.of());
        when(repository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(request);

        ArgumentCaptor<Long> excludeId = ArgumentCaptor.forClass(Long.class);
        verify(repository).findConflicts(
                any(), any(), any(), any(), any(), any(), excludeId.capture());
        assertThat(excludeId.getValue()).isEqualTo(-1L);
    }

    @Test
    void update_excludesSelfFromConflictCheck() {
        Lesson existing = new Lesson(
                "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.findConflicts(any(), any(), any(), any(), any(), any(), anyLong()))
                .thenReturn(List.of());

        service.update(42L, request);

        ArgumentCaptor<Long> excludeId = ArgumentCaptor.forClass(Long.class);
        verify(repository).findConflicts(
                any(), any(), any(), any(), any(), any(), excludeId.capture());
        assertThat(excludeId.getValue()).isEqualTo(42L);
    }

    @Test
    void get_throwsNotFound_whenMissing() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(7L))
                .isInstanceOf(LessonNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(LessonNotFoundException.class);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    void findAll_returnsLessonsSortedByDayThenStartTime() {
        Lesson tuesdayLate = new Lesson(
                "History", "Sidorov", "G-102", "C-1",
                DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(13, 0));
        Lesson mondayLate = new Lesson(
                "Physics", "Petrov", "G-103", "B-2",
                DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(13, 0));
        Lesson mondayEarly = new Lesson(
                "Math", "Ivanov", "G-101", "A-1",
                DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(11, 0));

        when(repository.findAll(ArgumentMatchers.<Specification<Lesson>>any()))
                .thenReturn(List.of(tuesdayLate, mondayLate, mondayEarly));

        List<LessonResponse> result = service.findAll(null, null, null);

        assertThat(result)
                .extracting(LessonResponse::subject)
                .containsExactly("Math", "Physics", "History");
    }
}
