package com.timetable.backend.controller;

import com.timetable.backend.dto.request.AcademicYearRequestDTO;
import com.timetable.backend.dto.response.AcademicYearResponseDTO;
import com.timetable.backend.service.AcademicYearService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-years")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    public AcademicYearController(AcademicYearService academicYearService) {
        this.academicYearService = academicYearService;
    }

    @GetMapping
    public ResponseEntity<List<AcademicYearResponseDTO>> getAllAcademicYears() {
        return ResponseEntity.ok(academicYearService.getAllAcademicYears());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicYearResponseDTO> getAcademicYearById(@PathVariable Long id) {
        return ResponseEntity.ok(academicYearService.getAcademicYearById(id));
    }

    @PostMapping
    public ResponseEntity<AcademicYearResponseDTO> createAcademicYear(@Valid @RequestBody AcademicYearRequestDTO dto) {
        AcademicYearResponseDTO created = academicYearService.createAcademicYear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicYearResponseDTO> updateAcademicYear(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequestDTO dto) {
        AcademicYearResponseDTO updated = academicYearService.updateAcademicYear(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.noContent().build();
    }
}
