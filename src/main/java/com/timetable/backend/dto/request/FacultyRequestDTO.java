package com.timetable.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FacultyRequestDTO {

    @NotBlank(message = "Faculty name is required")
    private String name;

    @NotBlank(message = "Designation is required")
    @Size(max = 50, message = "Designation must be at most 50 characters")
    private String designation;

    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Max weekly hours is required")
    @Min(value = 1, message = "Max weekly hours must be at least 1")
    private Integer maxWeeklyHours = 18;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public FacultyRequestDTO() {
    }

    public FacultyRequestDTO(String name, String designation, String email, Integer maxWeeklyHours, Long departmentId) {
        this.name = name;
        this.designation = designation;
        this.email = email;
        this.maxWeeklyHours = maxWeeklyHours;
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(Integer maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
