package com.timetable.backend.dto.request;

import com.timetable.backend.model.enums.SubjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SubjectRequestDTO {

    @Size(max = 30, message = "Code must be at most 30 characters")
    private String code;

    @NotBlank(message = "Subject name is required")
    private String name;

    @NotBlank(message = "Short name is required")
    @Size(max = 20, message = "Short name must be at most 20 characters")
    private String shortName;

    @NotNull(message = "Subject type is required")
    private SubjectType type = SubjectType.THEORY;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Semester ID is required")
    private Long semesterId;

    private Long electiveGroupId;

    public SubjectRequestDTO() {
    }

    public SubjectRequestDTO(String code, String name, String shortName, SubjectType type, Long departmentId, Long semesterId, Long electiveGroupId) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.departmentId = departmentId;
        this.semesterId = semesterId;
        this.electiveGroupId = electiveGroupId;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public SubjectType getType() {
        return type;
    }

    public void setType(SubjectType type) {
        this.type = type;
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

    public Long getElectiveGroupId() {
        return electiveGroupId;
    }

    public void setElectiveGroupId(Long electiveGroupId) {
        this.electiveGroupId = electiveGroupId;
    }
}
