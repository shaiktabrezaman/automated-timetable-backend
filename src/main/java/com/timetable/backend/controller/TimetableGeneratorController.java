package com.timetable.backend.controller;

import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/timetable-generator")
public class TimetableGeneratorController {

    private final SchedulerService schedulerService;

    public TimetableGeneratorController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/generate/{sectionId}")
    public ResponseEntity<List<TimetableSlotResponseDTO>> generateTimetable(@PathVariable Long sectionId) {
        List<TimetableSlotResponseDTO> generatedTimetable = schedulerService.generateTimetable(sectionId);
        return ResponseEntity.ok(generatedTimetable);
    }
}
