package com.timetable.backend.controller;

import com.timetable.backend.dto.request.SubjectRequestDTO;
import com.timetable.backend.dto.response.SubjectResponseDTO;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) SubjectType type) {
        return ResponseEntity.ok(subjectService.getAllSubjects(departmentId, semesterId, type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> getSubjectById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PostMapping
    public ResponseEntity<SubjectResponseDTO> createSubject(@Valid @RequestBody SubjectRequestDTO dto) {
        SubjectResponseDTO created = subjectService.createSubject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDTO> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequestDTO dto) {
        SubjectResponseDTO updated = subjectService.updateSubject(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
