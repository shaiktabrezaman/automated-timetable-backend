package com.timetable.backend.controller;

import com.timetable.backend.dto.request.ElectiveGroupRequestDTO;
import com.timetable.backend.dto.response.ElectiveGroupResponseDTO;
import com.timetable.backend.service.ElectiveGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elective-groups")
public class ElectiveGroupController {

    private final ElectiveGroupService electiveGroupService;

    public ElectiveGroupController(ElectiveGroupService electiveGroupService) {
        this.electiveGroupService = electiveGroupService;
    }

    @GetMapping
    public ResponseEntity<List<ElectiveGroupResponseDTO>> getAllElectiveGroups(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long semesterId) {
        return ResponseEntity.ok(electiveGroupService.getAllElectiveGroups(departmentId, semesterId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElectiveGroupResponseDTO> getElectiveGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(electiveGroupService.getElectiveGroupById(id));
    }

    @PostMapping
    public ResponseEntity<ElectiveGroupResponseDTO> createElectiveGroup(@Valid @RequestBody ElectiveGroupRequestDTO dto) {
        ElectiveGroupResponseDTO created = electiveGroupService.createElectiveGroup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ElectiveGroupResponseDTO> updateElectiveGroup(
            @PathVariable Long id,
            @Valid @RequestBody ElectiveGroupRequestDTO dto) {
        ElectiveGroupResponseDTO updated = electiveGroupService.updateElectiveGroup(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteElectiveGroup(@PathVariable Long id) {
        electiveGroupService.deleteElectiveGroup(id);
        return ResponseEntity.noContent().build();
    }
}
