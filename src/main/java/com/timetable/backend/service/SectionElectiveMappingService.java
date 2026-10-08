package com.timetable.backend.service;

import com.timetable.backend.dto.request.SectionElectiveMappingRequestDTO;
import com.timetable.backend.dto.response.SectionElectiveMappingResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.ElectiveGroup;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.SectionElectiveMapping;
import com.timetable.backend.model.Subject;
import com.timetable.backend.repository.SectionElectiveMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionElectiveMappingService {

    private final SectionElectiveMappingRepository mappingRepository;
    private final SectionService sectionService;
    private final ElectiveGroupService electiveGroupService;
    private final SubjectService subjectService;

    public SectionElectiveMappingService(SectionElectiveMappingRepository mappingRepository,
                                         SectionService sectionService,
                                         ElectiveGroupService electiveGroupService,
                                         SubjectService subjectService) {
        this.mappingRepository   = mappingRepository;
        this.sectionService      = sectionService;
        this.electiveGroupService = electiveGroupService;
        this.subjectService      = subjectService;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SectionElectiveMappingResponseDTO> getMappingsBySection(Long sectionId) {
        sectionService.findEntityById(sectionId); // validate exists
        return mappingRepository.findBySectionId(sectionId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SectionElectiveMappingResponseDTO> getMappingsByElectiveGroup(Long electiveGroupId) {
        electiveGroupService.findEntityById(electiveGroupId); // validate exists
        return mappingRepository.findByElectiveGroupId(electiveGroupId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectionElectiveMappingResponseDTO getMappingById(Long id) {
        return mapToDTO(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    public SectionElectiveMappingResponseDTO createMapping(SectionElectiveMappingRequestDTO dto) {
        Section        section        = sectionService.findEntityById(dto.getSectionId());
        ElectiveGroup  electiveGroup  = electiveGroupService.findEntityById(dto.getElectiveGroupId());
        Subject        selectedSubject = subjectService.findEntityById(dto.getSelectedSubjectId());

        validateSubjectBelongsToGroup(selectedSubject, electiveGroup);

        if (mappingRepository.existsBySectionIdAndElectiveGroupId(section.getId(), electiveGroup.getId())) {
            throw new ValidationException(
                    "Section '" + section.getCode() + "' already has an elective selection for group '"
                    + electiveGroup.getName() + "'.");
        }

        SectionElectiveMapping mapping = new SectionElectiveMapping(section, electiveGroup, selectedSubject);
        return mapToDTO(mappingRepository.save(mapping));
    }

    /**
     * Updates the selected subject for an existing section–elective-group mapping.
     * The section and elective group are immutable after creation; only the
     * selected subject may change (e.g., if a student batch switches elective choice).
     */
    public SectionElectiveMappingResponseDTO updateMapping(Long id, SectionElectiveMappingRequestDTO dto) {
        SectionElectiveMapping mapping = findEntityById(id);

        Section       section       = sectionService.findEntityById(dto.getSectionId());
        ElectiveGroup electiveGroup = electiveGroupService.findEntityById(dto.getElectiveGroupId());
        Subject       selectedSubject = subjectService.findEntityById(dto.getSelectedSubjectId());

        validateSubjectBelongsToGroup(selectedSubject, electiveGroup);

        // If the section+group pair is being changed, guard against creating a duplicate
        boolean sectionChanged = !mapping.getSection().getId().equals(section.getId());
        boolean groupChanged   = !mapping.getElectiveGroup().getId().equals(electiveGroup.getId());
        if (sectionChanged || groupChanged) {
            if (mappingRepository.existsBySectionIdAndElectiveGroupId(section.getId(), electiveGroup.getId())) {
                throw new ValidationException(
                        "Section '" + section.getCode() + "' already has an elective selection for group '"
                        + electiveGroup.getName() + "'.");
            }
        }

        mapping.setSection(section);
        mapping.setElectiveGroup(electiveGroup);
        mapping.setSelectedSubject(selectedSubject);

        return mapToDTO(mappingRepository.save(mapping));
    }

    public void deleteMapping(Long id) {
        mappingRepository.delete(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private SectionElectiveMapping findEntityById(Long id) {
        return mappingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section elective mapping not found with ID: " + id));
    }

    /**
     * Verifies that the chosen subject's elective group matches the requested group.
     * This prevents a subject from one elective group being assigned under a different group.
     */
    private void validateSubjectBelongsToGroup(Subject subject, ElectiveGroup group) {
        if (subject.getElectiveGroup() == null
                || !subject.getElectiveGroup().getId().equals(group.getId())) {
            throw new ValidationException(
                    "Subject '" + subject.getName() + "' does not belong to elective group '"
                    + group.getName() + "'.");
        }
    }

    // -------------------------------------------------------------------------
    // DTO mapping
    // -------------------------------------------------------------------------

    private SectionElectiveMappingResponseDTO mapToDTO(SectionElectiveMapping m) {
        return new SectionElectiveMappingResponseDTO(
                m.getId(),
                m.getSection().getId(),
                m.getSection().getCode(),
                m.getElectiveGroup().getId(),
                m.getElectiveGroup().getName(),
                m.getSelectedSubject().getId(),
                m.getSelectedSubject().getCode(),
                m.getSelectedSubject().getName()
        );
    }
}
