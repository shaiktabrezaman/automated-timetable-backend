package com.timetable.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SemesterRequestDTO {

    @NotNull(message = "Year number is required")
    @Min(value = 1, message = "Year number must be at least 1")
    private Integer yearNumber;

    @NotNull(message = "Semester number is required")
    @Min(value = 1, message = "Semester number must be at least 1")
    private Integer semesterNumber;

    @NotBlank(message = "Semester code is required")
    @Size(max = 20, message = "Code must be at most 20 characters")
    private String code;

    @NotBlank(message = "Semester name is required")
    private String name;

    public SemesterRequestDTO() {
    }

    public SemesterRequestDTO(Integer yearNumber, Integer semesterNumber, String code, String name) {
        this.yearNumber = yearNumber;
        this.semesterNumber = semesterNumber;
        this.code = code;
        this.name = name;
    }

    public Integer getYearNumber() {
        return yearNumber;
    }

    public void setYearNumber(Integer yearNumber) {
        this.yearNumber = yearNumber;
    }

    public Integer getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(Integer semesterNumber) {
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
