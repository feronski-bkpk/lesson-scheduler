package com.example.schedule.lesson;

import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonRepository extends JpaRepository<Lesson, Long>, JpaSpecificationExecutor<Lesson> {
    @Query("""
            select l from Lesson l
            where l.dayOfWeek = :DayOfWeek
            and l.id<> :excludeId
            and l.startTime < :endTime
            and :startTime < l.endTime
            and (l.groupName = :groupName
                or l.teacher = :teacher
                or l.room = :room)
                """)
    List<Lesson> findConflicts(
        @Param("dayOfWeek") DayOfWeek dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("groupName") String groupName,
        @Param("teacher") String teacher,
        @Param("room") String room,
        @Param("excludeId") Long excludeId);
}
