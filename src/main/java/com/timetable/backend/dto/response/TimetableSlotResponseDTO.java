package com.timetable.backend.dto.response;

import com.timetable.backend.model.enums.SubjectType;
import java.time.DayOfWeek;

public class TimetableSlotResponseDTO {
    private Long id;
    private Long sectionId;
    private String sectionCode;
    private DayOfWeek day;
    private int periodIndex;
    private int spanPeriods;
    private boolean isMultiPeriodHead;
    private boolean isMultiPeriodContinuation;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String subjectShortName;
    private SubjectType subjectType;

    private Long facultyId;
    private String facultyName;

    private Long roomId;
    private String roomNumber;

    public TimetableSlotResponseDTO() {
    }

    public TimetableSlotResponseDTO(Long id, Long sectionId, String sectionCode, DayOfWeek day, int periodIndex, int spanPeriods, boolean isMultiPeriodHead, boolean isMultiPeriodContinuation, Long subjectId, String subjectCode, String subjectName, String subjectShortName, SubjectType subjectType, Long facultyId, String facultyName, Long roomId, String roomNumber) {
        this.id = id;
        this.sectionId = sectionId;
        this.sectionCode = sectionCode;
        this.day = day;
        this.periodIndex = periodIndex;
        this.spanPeriods = spanPeriods;
        this.isMultiPeriodHead = isMultiPeriodHead;
        this.isMultiPeriodContinuation = isMultiPeriodContinuation;
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.subjectShortName = subjectShortName;
        this.subjectType = subjectType;
        this.facultyId = facultyId;
        this.facultyName = facultyName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
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

    public DayOfWeek getDay() {
        return day;
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public int getPeriodIndex() {
        return periodIndex;
    }

    public void setPeriodIndex(int periodIndex) {
        this.periodIndex = periodIndex;
    }

    public int getSpanPeriods() {
        return spanPeriods;
    }

    public void setSpanPeriods(int spanPeriods) {
        this.spanPeriods = spanPeriods;
    }

    public boolean isMultiPeriodHead() {
        return isMultiPeriodHead;
    }

    public void setMultiPeriodHead(boolean multiPeriodHead) {
        isMultiPeriodHead = multiPeriodHead;
    }

    public boolean isMultiPeriodContinuation() {
        return isMultiPeriodContinuation;
    }

    public void setMultiPeriodContinuation(boolean multiPeriodContinuation) {
        isMultiPeriodContinuation = multiPeriodContinuation;
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

    public Long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(Long facultyId) {
        this.facultyId = facultyId;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}
