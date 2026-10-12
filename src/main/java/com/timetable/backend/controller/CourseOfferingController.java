package com.timetable.backend.controller;

import com.timetable.backend.dto.request.CourseOfferingRequestDTO;
import com.timetable.backend.dto.response.CourseOfferingResponseDTO;
import com.timetable.backend.service.CourseOfferingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-offerings")
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;

    public CourseOfferingController(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    @GetMapping
    public ResponseEntity<List<CourseOfferingResponseDTO>> getAllCourseOfferings(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long facultyId) {
        return ResponseEntity.ok(courseOfferingService.getAllCourseOfferings(sectionId, subjectId, facultyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseOfferingResponseDTO> getCourseOfferingById(@PathVariable Long id) {
        return ResponseEntity.ok(courseOfferingService.getCourseOfferingById(id));
    }

    @GetMapping("/section/{sectionId}/active")
    public ResponseEntity<List<CourseOfferingResponseDTO>> getActiveOfferingsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(courseOfferingService.getActiveOfferingsBySection(sectionId));
    }

    @PostMapping
    public ResponseEntity<CourseOfferingResponseDTO> createCourseOffering(
            @Valid @RequestBody CourseOfferingRequestDTO dto) {
        CourseOfferingResponseDTO created = courseOfferingService.createCourseOffering(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseOfferingResponseDTO> updateCourseOffering(
            @PathVariable Long id,
            @Valid @RequestBody CourseOfferingRequestDTO dto) {
        CourseOfferingResponseDTO updated = courseOfferingService.updateCourseOffering(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourseOffering(@PathVariable Long id) {
        courseOfferingService.deleteCourseOffering(id);
        return ResponseEntity.noContent().build();
    }
}
