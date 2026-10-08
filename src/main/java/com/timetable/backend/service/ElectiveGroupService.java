package com.timetable.backend.service;

import com.timetable.backend.dto.request.ElectiveGroupRequestDTO;
import com.timetable.backend.dto.response.ElectiveGroupResponseDTO;
import com.timetable.backend.dto.response.SubjectResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.ElectiveGroup;
import com.timetable.backend.model.Semester;
import com.timetable.backend.repository.ElectiveGroupRepository;
import com.timetable.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ElectiveGroupService {

    private final ElectiveGroupRepository electiveGroupRepository;
    private final SubjectRepository subjectRepository;
    private final DepartmentService departmentService;
    private final SemesterService semesterService;
    private final SubjectService subjectService;

    public ElectiveGroupService(ElectiveGroupRepository electiveGroupRepository,
                                SubjectRepository subjectRepository,
                                DepartmentService departmentService,
                                SemesterService semesterService,
                                SubjectService subjectService) {
        this.electiveGroupRepository = electiveGroupRepository;
        this.subjectRepository       = subjectRepository;
        this.departmentService       = departmentService;
        this.semesterService         = semesterService;
        this.subjectService          = subjectService;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ElectiveGroupResponseDTO> getAllElectiveGroups(Long departmentId, Long semesterId) {
        List<ElectiveGroup> groups;
        if (departmentId != null && semesterId != null) {
            groups = electiveGroupRepository.findByDepartmentIdAndSemesterId(departmentId, semesterId);
        } else if (departmentId != null) {
            groups = electiveGroupRepository.findByDepartmentId(departmentId);
        } else if (semesterId != null) {
            groups = electiveGroupRepository.findBySemesterId(semesterId);
        } else {
            groups = electiveGroupRepository.findAll();
        }
        return groups.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ElectiveGroupResponseDTO getElectiveGroupById(Long id) {
        return mapToDTO(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    public ElectiveGroupResponseDTO createElectiveGroup(ElectiveGroupRequestDTO dto) {
        Department dept = departmentService.findEntityById(dto.getDepartmentId());
        Semester   sem  = semesterService.findEntityById(dto.getSemesterId());

        ElectiveGroup group = new ElectiveGroup(dto.getName().trim(), dept, sem);
        return mapToDTO(electiveGroupRepository.save(group));
    }

    public ElectiveGroupResponseDTO updateElectiveGroup(Long id, ElectiveGroupRequestDTO dto) {
        ElectiveGroup group = findEntityById(id);
        Department dept     = departmentService.findEntityById(dto.getDepartmentId());
        Semester   sem      = semesterService.findEntityById(dto.getSemesterId());

        group.setName(dto.getName().trim());
        group.setDepartment(dept);
        group.setSemester(sem);

        return mapToDTO(electiveGroupRepository.save(group));
    }

    public void deleteElectiveGroup(Long id) {
        ElectiveGroup group = findEntityById(id);
        electiveGroupRepository.delete(group);
    }

    // -------------------------------------------------------------------------
    // Package-visible helper — used by SectionElectiveMappingService
    // -------------------------------------------------------------------------

    public ElectiveGroup findEntityById(Long id) {
        return electiveGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Elective Group not found with ID: " + id));
    }

    // -------------------------------------------------------------------------
    // DTO mapping
    // -------------------------------------------------------------------------

    private ElectiveGroupResponseDTO mapToDTO(ElectiveGroup group) {
        // Load all subjects that belong to this elective group
        List<SubjectResponseDTO> electiveSubjects = subjectRepository
                .findByElectiveGroupId(group.getId())
                .stream()
                .map(subjectService::mapToDTO)
                .collect(Collectors.toList());

        return new ElectiveGroupResponseDTO(
                group.getId(),
                group.getName(),
                group.getDepartment().getId(),
                group.getDepartment().getCode(),
                group.getSemester().getId(),
                group.getSemester().getCode(),
                electiveSubjects
        );
    }
}
