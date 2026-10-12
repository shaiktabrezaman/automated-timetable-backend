package com.timetable.backend.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;

@Entity
@Table(name = "timetable_slots", uniqueConstraints = {
    @UniqueConstraint(name = "uk_section_day_period", columnNames = {"section_id", "day", "period_index"}),
    @UniqueConstraint(name = "uk_faculty_day_period", columnNames = {"faculty_id", "day", "period_index"}),
    @UniqueConstraint(name = "uk_room_day_period", columnNames = {"room_id", "day", "period_index"})
})
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DayOfWeek day;

    @Column(name = "period_index", nullable = false)
    private int periodIndex; // 1 to 7

    @Column(nullable = false)
    private int spanPeriods = 1; // 1, 2, or 3

    @Column(nullable = false)
    private boolean isMultiPeriodHead = false;

    @Column(nullable = false)
    private boolean isMultiPeriodContinuation = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public TimetableSlot() {
    }

    public TimetableSlot(Section section, DayOfWeek day, int periodIndex, int spanPeriods, boolean isMultiPeriodHead, boolean isMultiPeriodContinuation, Subject subject, Faculty faculty, Room room) {
        this.section = section;
        this.day = day;
        this.periodIndex = periodIndex;
        this.spanPeriods = spanPeriods;
        this.isMultiPeriodHead = isMultiPeriodHead;
        this.isMultiPeriodContinuation = isMultiPeriodContinuation;
        this.subject = subject;
        this.faculty = faculty;
        this.room = room;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
