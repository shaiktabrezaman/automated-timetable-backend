package com.timetable.backend.controller;

import com.timetable.backend.dto.request.FacultyRequestDTO;
import com.timetable.backend.dto.response.FacultyResponseDTO;
import com.timetable.backend.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    public ResponseEntity<List<FacultyResponseDTO>> getAllFaculty(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(facultyService.getAllFaculty(departmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacultyResponseDTO> getFacultyById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PostMapping
    public ResponseEntity<FacultyResponseDTO> createFaculty(@Valid @RequestBody FacultyRequestDTO dto) {
        FacultyResponseDTO created = facultyService.createFaculty(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacultyResponseDTO> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyRequestDTO dto) {
        FacultyResponseDTO updated = facultyService.updateFaculty(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long id) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }
}
