package com.timetable.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ElectiveGroupRequestDTO {

    @NotBlank(message = "Elective group name is required")
    private String name;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    public ElectiveGroupRequestDTO() {
    }

    public ElectiveGroupRequestDTO(String name, Long departmentId, Long semesterId) {
        this.name = name;
        this.departmentId = departmentId;
        this.semesterId = semesterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }
}
