package com.timetable.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class SectionElectiveMappingRequestDTO {

    @NotNull(message = "Section ID is required")
    private Long sectionId;

    @NotNull(message = "Elective group ID is required")
    private Long electiveGroupId;

    @NotNull(message = "Selected subject ID is required")
    private Long selectedSubjectId;

    public SectionElectiveMappingRequestDTO() {
    }

    public SectionElectiveMappingRequestDTO(Long sectionId, Long electiveGroupId, Long selectedSubjectId) {
        this.sectionId = sectionId;
        this.electiveGroupId = electiveGroupId;
        this.selectedSubjectId = selectedSubjectId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getElectiveGroupId() {
        return electiveGroupId;
    }

    public void setElectiveGroupId(Long electiveGroupId) {
        this.electiveGroupId = electiveGroupId;
    }

    public Long getSelectedSubjectId() {
        return selectedSubjectId;
    }

    public void setSelectedSubjectId(Long selectedSubjectId) {
        this.selectedSubjectId = selectedSubjectId;
    }
}
