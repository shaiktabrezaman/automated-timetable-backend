package com.timetable.backend.repository;

import com.timetable.backend.model.Subject;
import com.timetable.backend.model.enums.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByDepartmentIdAndSemesterId(Long departmentId, Long semesterId);
    List<Subject> findBySemesterId(Long semesterId);
    List<Subject> findByType(SubjectType type);
    List<Subject> findByElectiveGroupId(Long electiveGroupId);
}
