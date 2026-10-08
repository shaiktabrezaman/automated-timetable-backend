package com.timetable.backend.dto.response;

public class SectionElectiveMappingResponseDTO {
    private Long id;
    private Long sectionId;
    private String sectionCode;
    private Long electiveGroupId;
    private String electiveGroupName;
    private Long selectedSubjectId;
    private String selectedSubjectCode;
    private String selectedSubjectName;

    public SectionElectiveMappingResponseDTO() {
    }

    public SectionElectiveMappingResponseDTO(Long id, Long sectionId, String sectionCode, Long electiveGroupId, String electiveGroupName, Long selectedSubjectId, String selectedSubjectCode, String selectedSubjectName) {
        this.id = id;
        this.sectionId = sectionId;
        this.sectionCode = sectionCode;
        this.electiveGroupId = electiveGroupId;
        this.electiveGroupName = electiveGroupName;
        this.selectedSubjectId = selectedSubjectId;
        this.selectedSubjectCode = selectedSubjectCode;
        this.selectedSubjectName = selectedSubjectName;
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

    public Long getElectiveGroupId() {
        return electiveGroupId;
    }

    public void setElectiveGroupId(Long electiveGroupId) {
        this.electiveGroupId = electiveGroupId;
    }

    public String getElectiveGroupName() {
        return electiveGroupName;
    }

    public void setElectiveGroupName(String electiveGroupName) {
        this.electiveGroupName = electiveGroupName;
    }

    public Long getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(Long selectedSubjectId) {
        this.selectedSubjectId = selectedSubjectId;
    }

    public String getSelectedSubjectCode() {
        return selectedSubjectCode;
    }

    public void setSelectedSubjectCode(String selectedSubjectCode) {
        this.selectedSubjectCode = selectedSubjectCode;
    }

    public String getSelectedSubjectName() {
        return selectedSubjectName;
    }

    public void setSelectedSubjectName(String selectedSubjectName) {
        this.selectedSubjectName = selectedSubjectName;
    }
}
