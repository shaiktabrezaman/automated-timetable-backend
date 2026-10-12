package com.timetable.backend.service;

import com.timetable.backend.dto.request.AcademicYearRequestDTO;
import com.timetable.backend.dto.response.AcademicYearResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.AcademicYear;
import com.timetable.backend.repository.AcademicYearRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    public AcademicYearService(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    @Transactional(readOnly = true)
    public List<AcademicYearResponseDTO> getAllAcademicYears() {
        return academicYearRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicYearResponseDTO getAcademicYearById(Long id) {
        AcademicYear ay = findEntityById(id);
        return mapToDTO(ay);
    }

    public AcademicYearResponseDTO createAcademicYear(AcademicYearRequestDTO dto) {
        if (academicYearRepository.existsByYearName(dto.getYearName())) {
            throw new ValidationException("Academic Year '" + dto.getYearName() + "' already exists");
        }
        if (dto.isCurrent()) {
            clearCurrentFlags();
        }
        AcademicYear ay = new AcademicYear(dto.getYearName().trim(), dto.isCurrent());
        AcademicYear saved = academicYearRepository.save(ay);
        return mapToDTO(saved);
    }

    public AcademicYearResponseDTO updateAcademicYear(Long id, AcademicYearRequestDTO dto) {
        AcademicYear ay = findEntityById(id);
        if (!ay.getYearName().equalsIgnoreCase(dto.getYearName()) && academicYearRepository.existsByYearName(dto.getYearName())) {
            throw new ValidationException("Academic Year '" + dto.getYearName() + "' already exists");
        }
        if (dto.isCurrent() && !ay.isCurrent()) {
            clearCurrentFlags();
        }
        ay.setYearName(dto.getYearName().trim());
        ay.setCurrent(dto.isCurrent());
        AcademicYear updated = academicYearRepository.save(ay);
        return mapToDTO(updated);
    }

    public void deleteAcademicYear(Long id) {
        AcademicYear ay = findEntityById(id);
        academicYearRepository.delete(ay);
    }

    public AcademicYear findEntityById(Long id) {
        return academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic Year not found with ID: " + id));
    }

    private void clearCurrentFlags() {
        academicYearRepository.findByIsCurrentTrue().ifPresent(current -> {
            current.setCurrent(false);
            academicYearRepository.save(current);
        });
    }

    private AcademicYearResponseDTO mapToDTO(AcademicYear ay) {
        return new AcademicYearResponseDTO(ay.getId(), ay.getYearName(), ay.isCurrent());
    }
}
