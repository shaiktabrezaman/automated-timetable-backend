package com.timetable.backend.repository;

import com.timetable.backend.model.Subject;
import com.timetable.backend.model.enums.SubjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByDepartmentIdAndSemesterId(Long departmentId, Long semesterId);
    List<Subject> findBySemesterId(Long semesterId);
    List<Subject> findByType(SubjectType type);
    Optional<Subject> findByCode(String code);
    List<Subject> findByElectiveGroupId(Long electiveGroupId);
}
