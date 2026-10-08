package com.timetable.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;

public class SlotAssignRequestDTO {

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Day is required")
    private DayOfWeek day;

    @NotNull(message = "Period index is required (1 to 7)")
    @Min(value = 1, message = "Period must be between 1 and 7")
    @Max(value = 7, message = "Period must be between 1 and 7")
    private Integer periodIndex;

    @NotNull(message = "Span periods is required (1 to 3)")
    @Min(value = 1, message = "Span periods must be at least 1")
    @Max(value = 3, message = "Span periods must be at most 3")
    private Integer spanPeriods = 1;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    private Long facultyId;

    private Long roomId;

    public SlotAssignRequestDTO() {
    }

    public SlotAssignRequestDTO(Long sectionId, DayOfWeek day, Integer periodIndex, Integer spanPeriods, Long subjectId, Long facultyId, Long roomId) {
        this.sectionId = sectionId;
        this.day = day;
        this.periodIndex = periodIndex;
        this.spanPeriods = spanPeriods;
        this.subjectId = subjectId;
        this.facultyId = facultyId;
        this.roomId = roomId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public Integer getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(Integer periodIndex) {
        this.periodIndex = periodIndex;
    }

    public Integer getSpanPeriods() {
        return spanPeriods;
    }

    public void setSpanPeriods(Integer spanPeriods) {
        this.spanPeriods = spanPeriods;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
