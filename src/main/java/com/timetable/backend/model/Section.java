package com.timetable.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections", uniqueConstraints = {
    @UniqueConstraint(name = "uk_dept_ay_sem_code", columnNames = {"department_id", "academic_year_id", "semester_id", "code"})
})
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int studentCount = 60;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_room_id")
    private Room defaultRoom;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseOffering> courseOfferings = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimetableSlot> timetableSlots = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionElectiveMapping> electiveMappings = new ArrayList<>();

    public Section() {
    }

    public Section(String code, String name, int studentCount, Department department, AcademicYear academicYear, Semester semester, Room defaultRoom) {
        this.code = code;
        this.name = name;
        this.studentCount = studentCount;
        this.department = department;
        this.academicYear = academicYear;
        this.semester = semester;
        this.defaultRoom = defaultRoom;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public Room getDefaultRoom() {
        return defaultRoom;
    }

    public void setDefaultRoom(Room defaultRoom) {
        this.defaultRoom = defaultRoom;
    }

    public List<CourseOffering> getCourseOfferings() {
        return courseOfferings;
    }

    public void setCourseOfferings(List<CourseOffering> courseOfferings) {
        this.courseOfferings = courseOfferings;
    }

    public List<TimetableSlot> getTimetableSlots() {
        return timetableSlots;
    }

    public void setTimetableSlots(List<TimetableSlot> timetableSlots) {
        this.timetableSlots = timetableSlots;
    }

    public List<SectionElectiveMapping> getElectiveMappings() {
        return electiveMappings;
    }

    public void setElectiveMappings(List<SectionElectiveMapping> electiveMappings) {
        this.electiveMappings = electiveMappings;
    }
}
