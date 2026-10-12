package com.timetable.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "course_offerings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_section_subject", columnNames = {"section_id", "subject_id"})
})
public class CourseOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    /**
     * Total number of timetable periods required per week.
     */
    @Column(nullable = false)
    @Min(1)
    private int weeklyPeriods = 4;

    /**
     * Number of consecutive periods occupied per single meeting/session.
     * e.g., 1 for Theory, 2 for Technical Training, 3 for Laboratory.
     */
    @Column(nullable = false)
    @Min(1)
    private int durationPeriods = 1;

    /**
     * Assigned faculty member (nullable if faculty assignment is pending).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_faculty_id")
    private Faculty assignedFaculty;

    /**
     * Preferred room or specialized laboratory (nullable).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_room_id")
    private Room preferredRoom;

    @Column(nullable = false)
    private boolean active = true;

    public CourseOffering() {
    }

    public CourseOffering(Section section, Subject subject, int weeklyPeriods, int durationPeriods, Faculty assignedFaculty, Room preferredRoom, boolean active) {
        this.section = section;
        this.subject = subject;
        this.weeklyPeriods = weeklyPeriods;
        this.durationPeriods = durationPeriods;
        this.assignedFaculty = assignedFaculty;
        this.preferredRoom = preferredRoom;
        this.active = active;
    }

    @AssertTrue(message = "weeklyPeriods must be greater than or equal to durationPeriods and divisible by durationPeriods")
    public boolean isValidPeriodConfiguration() {
        return weeklyPeriods > 0 && durationPeriods > 0 && weeklyPeriods >= durationPeriods && (weeklyPeriods % durationPeriods == 0);
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
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

    public Faculty getAssignedFaculty() {
        return assignedFaculty;
    }

    public void setAssignedFaculty(Faculty assignedFaculty) {
        this.assignedFaculty = assignedFaculty;
    }

    public Room getPreferredRoom() {
        return preferredRoom;
    }

    public void setPreferredRoom(Room preferredRoom) {
        this.preferredRoom = preferredRoom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
