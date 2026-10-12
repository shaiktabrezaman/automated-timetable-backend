package com.timetable.backend.repository;

import com.timetable.backend.model.CourseOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseOfferingRepository extends JpaRepository<CourseOffering, Long> {
    List<CourseOffering> findBySectionId(Long sectionId);
    List<CourseOffering> findBySectionIdAndActiveTrue(Long sectionId);
    List<CourseOffering> findBySubjectId(Long subjectId);
    List<CourseOffering> findByAssignedFacultyId(Long facultyId);
    Optional<CourseOffering> findBySectionIdAndSubjectId(Long sectionId, Long subjectId);
    boolean existsBySectionIdAndSubjectId(Long sectionId, Long subjectId);
}
