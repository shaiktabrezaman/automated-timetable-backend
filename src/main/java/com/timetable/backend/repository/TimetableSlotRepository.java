package com.timetable.backend.repository;

import com.timetable.backend.model.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {
    List<TimetableSlot> findBySectionId(Long sectionId);
    List<TimetableSlot> findBySectionIdOrderByDayAscPeriodIndexAsc(Long sectionId);
    List<TimetableSlot> findByFacultyId(Long facultyId);
    List<TimetableSlot> findByRoomId(Long roomId);

    Optional<TimetableSlot> findBySectionIdAndDayAndPeriodIndex(Long sectionId, DayOfWeek day, int periodIndex);

    boolean existsBySectionIdAndDayAndPeriodIndex(Long sectionId, DayOfWeek day, int periodIndex);
    boolean existsByFacultyIdAndDayAndPeriodIndex(Long facultyId, DayOfWeek day, int periodIndex);
    boolean existsByRoomIdAndDayAndPeriodIndex(Long roomId, DayOfWeek day, int periodIndex);

    long countByFacultyId(Long facultyId);
    long countBySectionIdAndSubjectId(Long sectionId, Long subjectId);

    void deleteBySectionId(Long sectionId);
}
