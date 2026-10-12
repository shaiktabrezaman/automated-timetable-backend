package com.timetable.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SectionRequestDTO {

    @NotBlank(message = "Section code is required")
    @Size(max = 30, message = "Code must be at most 30 characters")
    private String code;

    @NotBlank(message = "Section name is required")
    private String name;

    @NotNull(message = "Student count is required")
    @Min(value = 1, message = "Student count must be at least 1")
    private Integer studentCount = 60;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Academic year ID is required")
    private Long academicYearId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    private Long defaultRoomId;

    public SectionRequestDTO() {
    }

    public SectionRequestDTO(String code, String name, Integer studentCount, Long departmentId, Long academicYearId, Long semesterId, Long defaultRoomId) {
        this.code = code;
        this.name = name;
        this.studentCount = studentCount;
        this.departmentId = departmentId;
        this.academicYearId = academicYearId;
        this.semesterId = semesterId;
        this.defaultRoomId = defaultRoomId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Long getDefaultRoomId() {
        return defaultRoomId;
    }

    public void setDefaultRoomId(Long defaultRoomId) {
        this.defaultRoomId = defaultRoomId;
    }
}
