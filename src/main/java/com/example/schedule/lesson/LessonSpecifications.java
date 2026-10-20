package com.example.schedule.lesson;

import org.springframework.data.jpa.domain.Specification;

public final class LessonSpecifications {

    private LessonSpecifications() {
    }

    public static Specification<Lesson> hasGroupName(String groupName) {
        return (root, query, cb) -> groupName == null
                ? cb.conjunction()
                : cb.equal(root.get("groupName"), groupName);
    }

    public static Specification<Lesson> hasTeacher(String teacher) {
        return (root, query, cb) -> teacher == null
                ? cb.conjunction()
                : cb.equal(root.get("teacher"), teacher);
    }

    public static Specification<Lesson> hasDayOfWeek(DayOfWeek dayOfWeek) {
        return (root, query, cb) -> dayOfWeek == null
                ? cb.conjunction()
                : cb.equal(root.get("dayOfWeek"), dayOfWeek);
    }
}
