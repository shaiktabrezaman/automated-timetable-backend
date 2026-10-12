package com.timetable.backend.dto.response;

import com.timetable.backend.model.enums.SubjectType;

public class CourseOfferingResponseDTO {
    private Long id;
    private Long sectionId;
    private String sectionCode;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String subjectShortName;
    private SubjectType subjectType;
    private int weeklyPeriods;
    private int durationPeriods;
    private Long assignedFacultyId;
    private String assignedFacultyName;
    private Long preferredRoomId;
    private String preferredRoomNumber;
    private boolean active;

    public CourseOfferingResponseDTO() {
    }

    public CourseOfferingResponseDTO(Long id, Long sectionId, String sectionCode, Long subjectId, String subjectCode, String subjectName, String subjectShortName, SubjectType subjectType, int weeklyPeriods, int durationPeriods, Long assignedFacultyId, String assignedFacultyName, Long preferredRoomId, String preferredRoomNumber, boolean active) {
        this.id = id;
        this.sectionId = sectionId;
        this.sectionCode = sectionCode;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.subjectShortName = subjectShortName;
        this.subjectType = subjectType;
        this.weeklyPeriods = weeklyPeriods;
        this.durationPeriods = durationPeriods;
        this.assignedFacultyId = assignedFacultyId;
        this.assignedFacultyName = assignedFacultyName;
        this.preferredRoomId = preferredRoomId;
        this.preferredRoomNumber = preferredRoomNumber;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectShortName() {
        return subjectShortName;
    }

    public void setSubjectShortName(String subjectShortName) {
        this.subjectShortName = subjectShortName;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public int getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public void setWeeklyPeriods(int weeklyPeriods) {
        this.weeklyPeriods = weeklyPeriods;
    }

    public int getDurationPeriods() {
        return durationPeriods;
    }

    public void setDurationPeriods(int durationPeriods) {
        this.durationPeriods = durationPeriods;
    }

    public Long getAssignedFacultyId() {
        return assignedFacultyId;
    }

    public void setAssignedFacultyId(Long assignedFacultyId) {
        this.assignedFacultyId = assignedFacultyId;
    }

    public String getAssignedFacultyName() {
        return assignedFacultyName;
    }

    public void setAssignedFacultyName(String assignedFacultyName) {
        this.assignedFacultyName = assignedFacultyName;
    }

    public Long getPreferredRoomId() {
        return preferredRoomId;
    }

    public void setPreferredRoomId(Long preferredRoomId) {
        this.preferredRoomId = preferredRoomId;
    }

    public String getPreferredRoomNumber() {
        return preferredRoomNumber;
    }

    public void setPreferredRoomNumber(String preferredRoomNumber) {
        this.preferredRoomNumber = preferredRoomNumber;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
