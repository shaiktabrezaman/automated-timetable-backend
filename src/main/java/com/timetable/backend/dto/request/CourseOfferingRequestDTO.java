package com.timetable.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CourseOfferingRequestDTO {

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    @NotNull(message = "Weekly periods is required")
    @Min(value = 1, message = "Weekly periods must be at least 1")
    private Integer weeklyPeriods = 4;

    @NotNull(message = "Duration periods is required")
    @Min(value = 1, message = "Duration periods must be at least 1")
    private Integer durationPeriods = 1;

    private Long assignedFacultyId;

    private Long preferredRoomId;

    private boolean active = true;

    public CourseOfferingRequestDTO() {
    }

    public CourseOfferingRequestDTO(Long sectionId, Long subjectId, Integer weeklyPeriods, Integer durationPeriods, Long assignedFacultyId, Long preferredRoomId, boolean active) {
        this.sectionId = sectionId;
        this.subjectId = subjectId;
        this.weeklyPeriods = weeklyPeriods;
        this.durationPeriods = durationPeriods;
        this.assignedFacultyId = assignedFacultyId;
        this.preferredRoomId = preferredRoomId;
        this.active = active;
    }

    @AssertTrue(message = "weeklyPeriods must be greater than or equal to durationPeriods and divisible by durationPeriods")
    public boolean isValidPeriodConfig() {
        if (weeklyPeriods == null || durationPeriods == null) return false;
        return weeklyPeriods >= durationPeriods && (weeklyPeriods % durationPeriods == 0);
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public void setWeeklyPeriods(Integer weeklyPeriods) {
        this.weeklyPeriods = weeklyPeriods;
    }

    public Integer getDurationPeriods() {
        return durationPeriods;
    }

    public void setDurationPeriods(Integer durationPeriods) {
        this.durationPeriods = durationPeriods;
    }

    public Long getAssignedFacultyId() {
        return assignedFacultyId;
    }

    public void setAssignedFacultyId(Long assignedFacultyId) {
        this.assignedFacultyId = assignedFacultyId;
    }

    public Long getPreferredRoomId() {
        return preferredRoomId;
    }

    public void setPreferredRoomId(Long preferredRoomId) {
        this.preferredRoomId = preferredRoomId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
