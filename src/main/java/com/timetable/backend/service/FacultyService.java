package com.timetable.backend.service;

import com.timetable.backend.dto.request.FacultyRequestDTO;
import com.timetable.backend.dto.response.FacultyResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.Faculty;
import com.timetable.backend.repository.FacultyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentService departmentService;

    public FacultyService(FacultyRepository facultyRepository, DepartmentService departmentService) {
        this.facultyRepository = facultyRepository;
        this.departmentService = departmentService;
    }

    @Transactional(readOnly = true)
    public List<FacultyResponseDTO> getAllFaculty(Long departmentId) {
        List<Faculty> facultyList;
        if (departmentId != null) {
            facultyList = facultyRepository.findByDepartmentId(departmentId);
        } else {
            facultyList = facultyRepository.findAll();
        }
        return facultyList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacultyResponseDTO getFacultyById(Long id) {
        Faculty f = findEntityById(id);
        return mapToDTO(f);
    }

    public FacultyResponseDTO createFaculty(FacultyRequestDTO dto) {
        Department dept = departmentService.findEntityById(dto.getDepartmentId());
        Faculty f = new Faculty(dto.getName().trim(), dto.getDesignation().trim(), dto.getEmail(), dto.getMaxWeeklyHours(), dept);
        Faculty saved = facultyRepository.save(f);
        return mapToDTO(saved);
    }

    public FacultyResponseDTO updateFaculty(Long id, FacultyRequestDTO dto) {
        Faculty f = findEntityById(id);
        Department dept = departmentService.findEntityById(dto.getDepartmentId());
        f.setName(dto.getName().trim());
        f.setDesignation(dto.getDesignation().trim());
        f.setEmail(dto.getEmail());
        f.setMaxWeeklyHours(dto.getMaxWeeklyHours());
        f.setDepartment(dept);
        Faculty updated = facultyRepository.save(f);
        return mapToDTO(updated);
    }

    public void deleteFaculty(Long id) {
        Faculty f = findEntityById(id);
        facultyRepository.delete(f);
    }

    public Faculty findEntityById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + id));
    }

    private FacultyResponseDTO mapToDTO(Faculty f) {
        return new FacultyResponseDTO(
                f.getId(),
                f.getName(),
                f.getDesignation(),
                f.getEmail(),
                f.getMaxWeeklyHours(),
                f.getDepartment().getId(),
                f.getDepartment().getCode()
        );
    }
}
