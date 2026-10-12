package com.timetable.backend.service;

import com.timetable.backend.dto.request.SemesterRequestDTO;
import com.timetable.backend.dto.response.SemesterResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.Semester;
import com.timetable.backend.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SemesterService {

    private final SemesterRepository semesterRepository;

    public SemesterService(SemesterRepository semesterRepository) {
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public List<SemesterResponseDTO> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterResponseDTO getSemesterById(Long id) {
        Semester sem = findEntityById(id);
        return mapToDTO(sem);
    }

    public SemesterResponseDTO createSemester(SemesterRequestDTO dto) {
        if (semesterRepository.existsByCode(dto.getCode())) {
            throw new ValidationException("Semester code '" + dto.getCode() + "' already exists");
        }
        Semester sem = new Semester(dto.getYearNumber(), dto.getSemesterNumber(), dto.getCode().trim().toUpperCase(), dto.getName().trim());
        Semester saved = semesterRepository.save(sem);
        return mapToDTO(saved);
    }

    public SemesterResponseDTO updateSemester(Long id, SemesterRequestDTO dto) {
        Semester sem = findEntityById(id);
        if (!sem.getCode().equalsIgnoreCase(dto.getCode()) && semesterRepository.existsByCode(dto.getCode())) {
            throw new ValidationException("Semester code '" + dto.getCode() + "' already exists");
        }
        sem.setYearNumber(dto.getYearNumber());
        sem.setSemesterNumber(dto.getSemesterNumber());
        sem.setCode(dto.getCode().trim().toUpperCase());
        sem.setName(dto.getName().trim());
        Semester updated = semesterRepository.save(sem);
        return mapToDTO(updated);
    }

    public void deleteSemester(Long id) {
        Semester sem = findEntityById(id);
        semesterRepository.delete(sem);
    }

    public Semester findEntityById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with ID: " + id));
    }

    private SemesterResponseDTO mapToDTO(Semester sem) {
        return new SemesterResponseDTO(sem.getId(), sem.getYearNumber(), sem.getSemesterNumber(), sem.getCode(), sem.getName());
    }
}
