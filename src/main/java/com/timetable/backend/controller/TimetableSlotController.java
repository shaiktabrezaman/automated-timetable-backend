package com.timetable.backend.controller;

import com.timetable.backend.dto.request.SlotAssignRequestDTO;
import com.timetable.backend.dto.response.TimetableGridResponseDTO;
import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.service.TimetableSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class TimetableSlotController {

    private final TimetableSlotService timetableSlotService;

    public TimetableSlotController(TimetableSlotService timetableSlotService) {
        this.timetableSlotService = timetableSlotService;
    }

    @PostMapping("/api/timetable-slots")
    public ResponseEntity<List<TimetableSlotResponseDTO>> assignSlot(
            @Valid @RequestBody SlotAssignRequestDTO dto) {
        List<TimetableSlotResponseDTO> createdSlots = timetableSlotService.assignSlot(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSlots);
    }

    @GetMapping("/api/timetable-slots")
    public ResponseEntity<List<TimetableSlotResponseDTO>> getSlots(
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) Long facultyId,
            @RequestParam(required = false) Long roomId) {
        if (sectionId != null) {
            return ResponseEntity.ok(timetableSlotService.getSlotsBySection(sectionId));
        } else if (facultyId != null) {
            return ResponseEntity.ok(timetableSlotService.getSlotsByFaculty(facultyId));
        } else if (roomId != null) {
            return ResponseEntity.ok(timetableSlotService.getSlotsByRoom(roomId));
        }
        return ResponseEntity.ok(timetableSlotService.getAllSlots());
    }

    @GetMapping("/api/timetable-slots/section/{sectionId}")
    public ResponseEntity<List<TimetableSlotResponseDTO>> getSlotsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(timetableSlotService.getSlotsBySection(sectionId));
    }

    @GetMapping("/api/timetable-slots/faculty/{facultyId}")
    public ResponseEntity<List<TimetableSlotResponseDTO>> getSlotsByFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(timetableSlotService.getSlotsByFaculty(facultyId));
    }

    @GetMapping("/api/timetable-slots/room/{roomId}")
    public ResponseEntity<List<TimetableSlotResponseDTO>> getSlotsByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(timetableSlotService.getSlotsByRoom(roomId));
    }

    @GetMapping({"/api/timetable-grid/{sectionId}", "/api/timetable-slots/grid/{sectionId}"})
    public ResponseEntity<TimetableGridResponseDTO> getTimetableGrid(@PathVariable Long sectionId) {
        return ResponseEntity.ok(timetableSlotService.getTimetableGrid(sectionId));
    }

    @GetMapping("/api/timetable-grid")
    public ResponseEntity<TimetableGridResponseDTO> getTimetableGridByParam(@RequestParam Long sectionId) {
        return ResponseEntity.ok(timetableSlotService.getTimetableGrid(sectionId));
    }

    @DeleteMapping("/api/timetable-slots/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        timetableSlotService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping({"/api/timetable-slots/section/{sectionId}", "/api/timetable-slots/clear/{sectionId}"})
    public ResponseEntity<Void> clearSectionTimetable(@PathVariable Long sectionId) {
        timetableSlotService.clearSectionTimetable(sectionId);
        return ResponseEntity.noContent().build();
    }
}
