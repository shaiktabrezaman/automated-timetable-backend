package com.timetable.backend.repository;

import com.timetable.backend.model.ElectiveGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElectiveGroupRepository extends JpaRepository<ElectiveGroup, Long> {
    List<ElectiveGroup> findByDepartmentId(Long departmentId);
    List<ElectiveGroup> findBySemesterId(Long semesterId);
    List<ElectiveGroup> findByDepartmentIdAndSemesterId(Long departmentId, Long semesterId);
}
