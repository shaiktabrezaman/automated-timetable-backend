package com.timetable.backend.repository;

import com.timetable.backend.model.SectionElectiveMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionElectiveMappingRepository extends JpaRepository<SectionElectiveMapping, Long> {
    List<SectionElectiveMapping> findBySectionId(Long sectionId);
    List<SectionElectiveMapping> findByElectiveGroupId(Long electiveGroupId);
    Optional<SectionElectiveMapping> findBySectionIdAndElectiveGroupId(Long sectionId, Long electiveGroupId);
    boolean existsBySectionIdAndElectiveGroupId(Long sectionId, Long electiveGroupId);
}
