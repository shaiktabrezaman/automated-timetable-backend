package com.timetable.backend.repository;

import com.timetable.backend.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByDepartmentId(Long departmentId);
    List<Section> findByDepartmentIdAndSemesterId(Long departmentId, Long semesterId);
    List<Section> findByDepartmentIdAndAcademicYearIdAndSemesterId(Long departmentId, Long academicYearId, Long semesterId);
    Optional<Section> findByDepartmentIdAndAcademicYearIdAndSemesterIdAndCode(Long departmentId, Long academicYearId, Long semesterId, String code);
}
