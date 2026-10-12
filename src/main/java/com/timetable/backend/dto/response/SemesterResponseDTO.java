package com.timetable.backend.dto.response;

public class SemesterResponseDTO {
    private Long id;
    private int yearNumber;
    private int semesterNumber;
    private String code;
    private String name;

    public SemesterResponseDTO() {
    }

    public SemesterResponseDTO(Long id, int yearNumber, int semesterNumber, String code, String name) {
        this.id = id;
        this.yearNumber = yearNumber;
        this.semesterNumber = semesterNumber;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getYearNumber() {
        return yearNumber;
    }

    public void setYearNumber(int yearNumber) {
        this.yearNumber = yearNumber;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
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
}
