package com.timetable.backend.model;

import com.timetable.backend.model.enums.SubjectType;
import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 20)
    private String shortName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubjectType type = SubjectType.THEORY;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elective_group_id")
    private ElectiveGroup electiveGroup;

    public Subject() {
    }

    public Subject(String code, String name, String shortName, SubjectType type, Department department, Semester semester, ElectiveGroup electiveGroup) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
        this.type = type;
        this.department = department;
        this.semester = semester;
        this.electiveGroup = electiveGroup;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public SubjectType getType() {
        return type;
    }

    public void setType(SubjectType type) {
        this.type = type;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public ElectiveGroup getElectiveGroup() {
        return electiveGroup;
    }

    public void setElectiveGroup(ElectiveGroup electiveGroup) {
        this.electiveGroup = electiveGroup;
    }
}
