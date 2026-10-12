package com.timetable.backend.dto.response;

public class AcademicYearResponseDTO {
    private Long id;
    private String yearName;
    private boolean isCurrent;

    public AcademicYearResponseDTO() {
    }

    public AcademicYearResponseDTO(Long id, String yearName, boolean isCurrent) {
        this.id = id;
        this.yearName = yearName;
        this.isCurrent = isCurrent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
