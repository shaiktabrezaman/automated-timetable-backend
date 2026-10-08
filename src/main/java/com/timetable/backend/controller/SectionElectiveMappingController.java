package com.timetable.backend.controller;

import com.timetable.backend.dto.request.SectionElectiveMappingRequestDTO;
import com.timetable.backend.dto.response.SectionElectiveMappingResponseDTO;
import com.timetable.backend.service.SectionElectiveMappingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/section-elective-mappings")
public class SectionElectiveMappingController {

    private final SectionElectiveMappingService mappingService;

    public SectionElectiveMappingController(SectionElectiveMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @GetMapping
    public ResponseEntity<List<SectionElectiveMappingResponseDTO>> getMappings(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long electiveGroupId) {
        if (sectionId != null) {
            return ResponseEntity.ok(mappingService.getMappingsBySection(sectionId));
        } else if (electiveGroupId != null) {
            return ResponseEntity.ok(mappingService.getMappingsByElectiveGroup(electiveGroupId));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SectionElectiveMappingResponseDTO> getMappingById(@PathVariable Long id) {
        return ResponseEntity.ok(mappingService.getMappingById(id));
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<SectionElectiveMappingResponseDTO>> getMappingsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(mappingService.getMappingsBySection(sectionId));
    }

    @GetMapping("/elective-group/{electiveGroupId}")
    public ResponseEntity<List<SectionElectiveMappingResponseDTO>> getMappingsByElectiveGroup(@PathVariable Long electiveGroupId) {
        return ResponseEntity.ok(mappingService.getMappingsByElectiveGroup(electiveGroupId));
    }

    @PostMapping
    public ResponseEntity<SectionElectiveMappingResponseDTO> createMapping(
            @Valid @RequestBody SectionElectiveMappingRequestDTO dto) {
        SectionElectiveMappingResponseDTO created = mappingService.createMapping(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SectionElectiveMappingResponseDTO> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody SectionElectiveMappingRequestDTO dto) {
        SectionElectiveMappingResponseDTO updated = mappingService.updateMapping(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Long id) {
        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }
}
