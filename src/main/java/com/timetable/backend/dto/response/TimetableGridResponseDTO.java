package com.timetable.backend.dto.response;

import java.util.ArrayList;
import java.util.List;

public class TimetableGridResponseDTO {
    private SectionResponseDTO section;
    private List<TimetableSlotResponseDTO> slots = new ArrayList<>();
    private List<CourseOfferingResponseDTO> courseOfferings = new ArrayList<>();

    public TimetableGridResponseDTO() {
    }

    public TimetableGridResponseDTO(SectionResponseDTO section, List<TimetableSlotResponseDTO> slots, List<CourseOfferingResponseDTO> courseOfferings) {
        this.section = section;
        this.slots = slots;
        this.courseOfferings = courseOfferings;
    }

    public SectionResponseDTO getSection() {
        return section;
    }

    public void setSection(SectionResponseDTO section) {
        this.section = section;
    }

    public List<TimetableSlotResponseDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<TimetableSlotResponseDTO> slots) {
        this.slots = slots;
    }

    public List<CourseOfferingResponseDTO> getCourseOfferings() {
        return courseOfferings;
    }

    public void setCourseOfferings(List<CourseOfferingResponseDTO> courseOfferings) {
        this.courseOfferings = courseOfferings;
    }
}
