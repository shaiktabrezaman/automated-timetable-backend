package com.timetable.backend.controller;

import com.timetable.backend.dto.request.SectionRequestDTO;
import com.timetable.backend.dto.response.SectionResponseDTO;
import com.timetable.backend.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ResponseEntity<List<SectionResponseDTO>> getAllSections(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long academicYearId,
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(sectionService.getAllSections(departmentId, academicYearId, semesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionResponseDTO> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PostMapping
    public ResponseEntity<SectionResponseDTO> createSection(@Valid @RequestBody SectionRequestDTO dto) {
        SectionResponseDTO created = sectionService.createSection(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionResponseDTO> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequestDTO dto) {
        SectionResponseDTO updated = sectionService.updateSection(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}
