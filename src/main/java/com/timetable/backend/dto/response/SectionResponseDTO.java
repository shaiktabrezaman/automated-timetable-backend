package com.timetable.backend.dto.response;

public class SectionResponseDTO {
    private Long id;
    private String code;
    private String name;
    private int studentCount;
    private Long departmentId;
    private String departmentCode;
    private Long academicYearId;
    private String academicYearName;
    private Long semesterId;
    private String semesterCode;
    private Long defaultRoomId;
    private String defaultRoomNumber;

    public SectionResponseDTO() {
    }

    public SectionResponseDTO(Long id, String code, String name, int studentCount, Long departmentId, String departmentCode, Long academicYearId, String academicYearName, Long semesterId, String semesterCode, Long defaultRoomId, String defaultRoomNumber) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.studentCount = studentCount;
        this.departmentId = departmentId;
        this.departmentCode = departmentCode;
        this.academicYearId = academicYearId;
        this.academicYearName = academicYearName;
        this.semesterId = semesterId;
        this.semesterCode = semesterCode;
        this.defaultRoomId = defaultRoomId;
        this.defaultRoomNumber = defaultRoomNumber;
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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
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

    public Long getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(Long academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getAcademicYearName() {
        return academicYearName;
    }

    public void setAcademicYearName(String academicYearName) {
        this.academicYearName = academicYearName;
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

    public Long getDefaultRoomId() {
        return defaultRoomId;
    }

    public void setDefaultRoomId(Long defaultRoomId) {
        this.defaultRoomId = defaultRoomId;
    }

    public String getDefaultRoomNumber() {
        return defaultRoomNumber;
    }

    public void setDefaultRoomNumber(String defaultRoomNumber) {
        this.defaultRoomNumber = defaultRoomNumber;
    }
}
