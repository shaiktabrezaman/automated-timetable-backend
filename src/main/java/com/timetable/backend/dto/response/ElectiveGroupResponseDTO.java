package com.timetable.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class ElectiveGroupResponseDTO {
    private Long id;
    private String name;
    private Long departmentId;
    private String departmentCode;
    private Long semesterId;
    private String semesterCode;
    private List<SubjectResponseDTO> electiveSubjects = new ArrayList<>();

    public ElectiveGroupResponseDTO() {
    }

    public ElectiveGroupResponseDTO(Long id, String name, Long departmentId, String departmentCode, Long semesterId, String semesterCode, List<SubjectResponseDTO> electiveSubjects) {
        this.id = id;
        this.name = name;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.semesterId = semesterId;
        this.semesterCode = semesterCode;
        this.electiveSubjects = electiveSubjects;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<SubjectResponseDTO> getElectiveSubjects() {
        return electiveSubjects;
    }

    public void setElectiveSubjects(List<SubjectResponseDTO> electiveSubjects) {
        this.electiveSubjects = electiveSubjects;
    }
}
