package com.timetable.backend.service;

import com.timetable.backend.dto.request.SubjectRequestDTO;
import com.timetable.backend.dto.response.SubjectResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.ElectiveGroup;
import com.timetable.backend.model.Semester;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.ElectiveGroupRepository;
import com.timetable.backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentService departmentService;
    private final SemesterService semesterService;
    private final ElectiveGroupRepository electiveGroupRepository;

    public SubjectService(SubjectRepository subjectRepository, DepartmentService departmentService, SemesterService semesterService, ElectiveGroupRepository electiveGroupRepository) {
        this.subjectRepository = subjectRepository;
        this.departmentService = departmentService;
        this.semesterService = semesterService;
        this.electiveGroupRepository = electiveGroupRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDTO> getAllSubjects(Long departmentId, Long semesterId, SubjectType type) {
        List<Subject> subjects;
        if (departmentId != null && semesterId != null) {
            subjects = subjectRepository.findByDepartmentIdAndSemesterId(departmentId, semesterId);
        } else if (departmentId != null) {
            subjects = subjectRepository.findByDepartmentId(departmentId);
        } else if (semesterId != null) {
            subjects = subjectRepository.findBySemesterId(semesterId);
        } else if (type != null) {
            subjects = subjectRepository.findByType(type);
        } else {
            subjects = subjectRepository.findAll();
        }
        return subjects.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectResponseDTO getSubjectById(Long id) {
        Subject s = findEntityById(id);
        return mapToDTO(s);
    }

    public SubjectResponseDTO createSubject(SubjectRequestDTO dto) {
        Department dept = departmentService.findEntityById(dto.getDepartmentId());
        Semester sem = semesterService.findEntityById(dto.getSemesterId());
        ElectiveGroup eg = null;
        if (dto.getElectiveGroupId() != null) {
            eg = electiveGroupRepository.findById(dto.getElectiveGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Elective Group not found with ID: " + dto.getElectiveGroupId()));
        }

        Subject subject = new Subject(
                dto.getCode() != null ? dto.getCode().trim().toUpperCase() : null,
                dto.getName().trim(),
                dto.getShortName().trim(),
                dto.getType(),
                dept,
                sem,
                eg
        );
        Subject saved = subjectRepository.save(subject);
        return mapToDTO(saved);
    }

    public SubjectResponseDTO updateSubject(Long id, SubjectRequestDTO dto) {
        Subject s = findEntityById(id);
        Department dept = departmentService.findEntityById(dto.getDepartmentId());
        Semester sem = semesterService.findEntityById(dto.getSemesterId());
        ElectiveGroup eg = null;
        if (dto.getElectiveGroupId() != null) {
            eg = electiveGroupRepository.findById(dto.getElectiveGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Elective Group not found with ID: " + dto.getElectiveGroupId()));
        }

        s.setCode(dto.getCode() != null ? dto.getCode().trim().toUpperCase() : null);
        s.setName(dto.getName().trim());
        s.setShortName(dto.getShortName().trim());
        s.setType(dto.getType());
        s.setDepartment(dept);
        s.setSemester(sem);
        s.setElectiveGroup(eg);

        Subject updated = subjectRepository.save(s);
        return mapToDTO(updated);
    }

    public void deleteSubject(Long id) {
        Subject s = findEntityById(id);
        subjectRepository.delete(s);
    }

    public Subject findEntityById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with ID: " + id));
    }

    public SubjectResponseDTO mapToDTO(Subject s) {
        return new SubjectResponseDTO(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getShortName(),
                s.getType(),
                s.getDepartment().getId(),
                s.getDepartment().getCode(),
                s.getSemester().getId(),
                s.getSemester().getCode(),
                s.getElectiveGroup() != null ? s.getElectiveGroup().getId() : null,
                s.getElectiveGroup() != null ? s.getElectiveGroup().getName() : null
        );
    }
}
