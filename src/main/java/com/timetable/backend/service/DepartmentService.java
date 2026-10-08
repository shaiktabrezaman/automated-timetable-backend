package com.timetable.backend.service;

import com.timetable.backend.dto.request.DepartmentRequestDTO;
import com.timetable.backend.dto.response.DepartmentResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.Department;
import com.timetable.backend.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department dept = findEntityById(id);
        return mapToDTO(dept);
    }

    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO dto) {
        if (departmentRepository.existsByCode(dto.getCode())) {
            throw new ValidationException("Department with code '" + dto.getCode() + "' already exists");
        }
        Department dept = new Department(dto.getCode().trim().toUpperCase(), dto.getName().trim());
        Department saved = departmentRepository.save(dept);
        return mapToDTO(saved);
    }

    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO dto) {
        Department dept = findEntityById(id);
        if (!dept.getCode().equalsIgnoreCase(dto.getCode()) && departmentRepository.existsByCode(dto.getCode())) {
            throw new ValidationException("Department with code '" + dto.getCode() + "' already exists");
        }
        dept.setCode(dto.getCode().trim().toUpperCase());
        dept.setName(dto.getName().trim());
        Department updated = departmentRepository.save(dept);
        return mapToDTO(updated);
    }

    public void deleteDepartment(Long id) {
        Department dept = findEntityById(id);
        departmentRepository.delete(dept);
    }

    public Department findEntityById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    private DepartmentResponseDTO mapToDTO(Department dept) {
        return new DepartmentResponseDTO(dept.getId(), dept.getCode(), dept.getName());
    }
}
