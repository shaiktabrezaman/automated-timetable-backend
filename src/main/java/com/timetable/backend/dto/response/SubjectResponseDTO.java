package com.timetable.backend.dto.response;

import com.timetable.backend.model.enums.SubjectType;

public class SubjectResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String shortName;
    private SubjectType type;
    private Long departmentId;
    private String departmentCode;
    private Long semesterId;
    private String semesterCode;
    private Long electiveGroupId;
    private String electiveGroupName;

    public SubjectResponseDTO() {
    }

    public SubjectResponseDTO(Long id, String code, String name, String shortName, SubjectType type, Long departmentId, String departmentCode, Long semesterId, String semesterCode, Long electiveGroupId, String electiveGroupName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.semesterId = semesterId;
        this.semesterCode = semesterCode;
        this.electiveGroupId = electiveGroupId;
        this.electiveGroupName = electiveGroupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public String getSemesterCode() {
        return semesterCode;
    }

    public void setSemesterCode(String semesterCode) {
        this.semesterCode = semesterCode;
    }

    public Long getElectiveGroupId() {
        return electiveGroupId;
    }

    public void setElectiveGroupId(Long electiveGroupId) {
        this.electiveGroupId = electiveGroupId;
    }

    public String getElectiveGroupName() {
        return electiveGroupName;
    }

    public void setElectiveGroupName(String electiveGroupName) {
        this.electiveGroupName = electiveGroupName;
    }
}
