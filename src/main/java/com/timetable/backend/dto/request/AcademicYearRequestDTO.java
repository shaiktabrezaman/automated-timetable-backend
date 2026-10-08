package com.timetable.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AcademicYearRequestDTO {

    @NotBlank(message = "Year name is required")
    @Size(max = 20, message = "Year name must be at most 20 characters")
    private String yearName;

    private boolean isCurrent;

    public AcademicYearRequestDTO() {
    }

    public AcademicYearRequestDTO(String yearName, boolean isCurrent) {
        this.yearName = yearName;
        this.isCurrent = isCurrent;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
