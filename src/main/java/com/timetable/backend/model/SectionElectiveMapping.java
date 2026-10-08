package com.timetable.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "section_elective_mappings", uniqueConstraints = {
    @UniqueConstraint(name = "uk_section_elective", columnNames = {"section_id", "elective_group_id"})
})
public class SectionElectiveMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elective_group_id", nullable = false)
    private ElectiveGroup electiveGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_subject_id", nullable = false)
    private Subject selectedSubject;

    public SectionElectiveMapping() {
    }

    public SectionElectiveMapping(Section section, ElectiveGroup electiveGroup, Subject selectedSubject) {
        this.section = section;
        this.electiveGroup = electiveGroup;
        this.selectedSubject = selectedSubject;
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

    public ElectiveGroup getElectiveGroup() {
        return electiveGroup;
    }

    public void setElectiveGroup(ElectiveGroup electiveGroup) {
        this.electiveGroup = electiveGroup;
    }

    public Subject getSelectedSubject() {
        return selectedSubject;
    }

    public void setSelectedSubject(Subject selectedSubject) {
        this.selectedSubject = selectedSubject;
    }
}
