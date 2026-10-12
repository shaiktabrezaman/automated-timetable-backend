package com.timetable.backend.dto.response;

public class FacultyResponseDTO {
    private Long id;
    private String name;
    private String designation;
    private String email;
    private int maxWeeklyHours;
    private Long departmentId;
    private String departmentCode;

    public FacultyResponseDTO() {
    }

    public FacultyResponseDTO(Long id, String name, String designation, String email, int maxWeeklyHours, Long departmentId, String departmentCode) {
        this.id = id;
        this.name = name;
        this.designation = designation;
        this.email = email;
        this.maxWeeklyHours = maxWeeklyHours;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
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

    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }

    public void setMaxWeeklyHours(int maxWeeklyHours) {
        this.maxWeeklyHours = maxWeeklyHours;
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
}
