package com.example.schedule.lesson;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.schedule.lesson.dto.LessonRequest;
import com.example.schedule.lesson.dto.LessonResponse;

@Service
@Transactional
public class LessonService {

    private static final long NO_EXCLUSION = -1L;

    private final LessonRepository repository;

    public LessonService(LessonRepository repository) {
        this.repository = repository;
    }

    public LessonResponse create(LessonRequest request) {
        ensureNoConflicts(request, NO_EXCLUSION);
        Lesson saved = repository.save(toEntity(request));
        return LessonResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public LessonResponse get(Long id) {
        return LessonResponse.from(findById(id));
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> findAll(String groupName, String teacher, DayOfWeek dayOfWeek) {
        Specification<Lesson> spec = Specification
                .where(LessonSpecifications.hasGroupName(groupName))
                .and(LessonSpecifications.hasTeacher(teacher))
                .and(LessonSpecifications.hasDayOfWeek(dayOfWeek));

        return repository.findAll(spec).stream()
                .sorted(Comparator.comparing(Lesson::getDayOfWeek)
                        .thenComparing(Lesson::getStartTime))
                .map(LessonResponse::from)
                .toList();
    }

    public LessonResponse update(Long id, LessonRequest request) {
        Lesson existing = findById(id);
        ensureNoConflicts(request, id);
        apply(existing, request);
        return LessonResponse.from(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new LessonNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private Lesson findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new LessonNotFoundException(id));
    }

    private void ensureNoConflicts(LessonRequest request, long excludeId) {
        List<Lesson> conflicts = repository.findConflicts(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.groupName(),
                request.teacher(),
                request.room(),
                excludeId
        );
        if (!conflicts.isEmpty()) {
            throw new LessonConflictException(buildConflictMessage(conflicts.get(0)));
        }
    }

    private String buildConflictMessage(Lesson conflict) {
        return "Conflict with existing lesson id=" + conflict.getId()
                + " (" + conflict.getGroupName() + ", "
                + conflict.getTeacher() + ", room " + conflict.getRoom()
                + ", " + conflict.getDayOfWeek()
                + " " + conflict.getStartTime() + "-" + conflict.getEndTime() + ")";
    }

    private Lesson toEntity(LessonRequest request) {
        return new Lesson(
                request.subject(),
                request.teacher(),
                request.groupName(),
                request.room(),
                request.dayOfWeek(),
                request.startTime(),
                request.endTime()
        );
    }

    private void apply(Lesson lesson, LessonRequest request) {
        lesson.setSubject(request.subject());
        lesson.setTeacher(request.teacher());
        lesson.setGroupName(request.groupName());
        lesson.setRoom(request.room());
        lesson.setDayOfWeek(request.dayOfWeek());
        lesson.setStartTime(request.startTime());
        lesson.setEndTime(request.endTime());
    }
}
