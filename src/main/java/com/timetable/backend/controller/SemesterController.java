package com.timetable.backend.controller;

import com.timetable.backend.dto.request.SemesterRequestDTO;
import com.timetable.backend.dto.response.SemesterResponseDTO;
import com.timetable.backend.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping
    public ResponseEntity<List<SemesterResponseDTO>> getAllSemesters() {
        return ResponseEntity.ok(semesterService.getAllSemesters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SemesterResponseDTO> getSemesterById(@PathVariable Long id) {
        return ResponseEntity.ok(semesterService.getSemesterById(id));
    }

    @PostMapping
    public ResponseEntity<SemesterResponseDTO> createSemester(@Valid @RequestBody SemesterRequestDTO dto) {
        SemesterResponseDTO created = semesterService.createSemester(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SemesterResponseDTO> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequestDTO dto) {
        SemesterResponseDTO updated = semesterService.updateSemester(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSemester(@PathVariable Long id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.noContent().build();
    }
}
