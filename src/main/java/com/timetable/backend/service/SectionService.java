package com.timetable.backend.service;

import com.timetable.backend.dto.request.SectionRequestDTO;
import com.timetable.backend.dto.response.SectionResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.AcademicYear;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.Semester;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.SectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SectionService {

    private final SectionRepository sectionRepository;
    private final DepartmentService departmentService;
    private final AcademicYearService academicYearService;
    private final SemesterService semesterService;
    private final RoomRepository roomRepository;

    public SectionService(SectionRepository sectionRepository,
                          DepartmentService departmentService,
                          AcademicYearService academicYearService,
                          SemesterService semesterService,
                          RoomRepository roomRepository) {
        this.sectionRepository = sectionRepository;
        this.departmentService = departmentService;
        this.academicYearService = academicYearService;
        this.semesterService = semesterService;
        this.roomRepository = roomRepository;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<SectionResponseDTO> getAllSections(Long departmentId, Long academicYearId, Long semesterId) {
        List<Section> sections;
        if (departmentId != null && academicYearId != null && semesterId != null) {
            sections = sectionRepository.findByDepartmentIdAndAcademicYearIdAndSemesterId(
                    departmentId, academicYearId, semesterId);
        } else if (departmentId != null && semesterId != null) {
            sections = sectionRepository.findByDepartmentIdAndSemesterId(departmentId, semesterId);
        } else if (departmentId != null) {
            sections = sectionRepository.findByDepartmentId(departmentId);
        } else {
            sections = sectionRepository.findAll();
        }
        return sections.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SectionResponseDTO getSectionById(Long id) {
        return mapToDTO(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    public SectionResponseDTO createSection(SectionRequestDTO dto) {
        Department dept       = departmentService.findEntityById(dto.getDepartmentId());
        AcademicYear ay       = academicYearService.findEntityById(dto.getAcademicYearId());
        Semester sem          = semesterService.findEntityById(dto.getSemesterId());
        Room defaultRoom      = resolveOptionalRoom(dto.getDefaultRoomId());

        String normalizedCode = dto.getCode().trim().toUpperCase();

        if (sectionRepository.findByDepartmentIdAndAcademicYearIdAndSemesterIdAndCode(
                dept.getId(), ay.getId(), sem.getId(), normalizedCode).isPresent()) {
            throw new ValidationException(
                    "Section with code '" + normalizedCode + "' already exists for this department, academic year and semester.");
        }

        Section section = new Section(normalizedCode, dto.getName().trim(), dto.getStudentCount(),
                dept, ay, sem, defaultRoom);
        return mapToDTO(sectionRepository.save(section));
    }

    public SectionResponseDTO updateSection(Long id, SectionRequestDTO dto) {
        Section section       = findEntityById(id);
        Department dept       = departmentService.findEntityById(dto.getDepartmentId());
        AcademicYear ay       = academicYearService.findEntityById(dto.getAcademicYearId());
        Semester sem          = semesterService.findEntityById(dto.getSemesterId());
        Room defaultRoom      = resolveOptionalRoom(dto.getDefaultRoomId());

        String normalizedCode = dto.getCode().trim().toUpperCase();

        // Uniqueness check: only reject if the duplicate is a *different* section
        sectionRepository.findByDepartmentIdAndAcademicYearIdAndSemesterIdAndCode(
                dept.getId(), ay.getId(), sem.getId(), normalizedCode)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(section.getId())) {
                        throw new ValidationException(
                                "Section with code '" + normalizedCode + "' already exists for this department, academic year and semester.");
                    }
                });

        section.setCode(normalizedCode);
        section.setName(dto.getName().trim());
        section.setStudentCount(dto.getStudentCount());
        section.setDepartment(dept);
        section.setAcademicYear(ay);
        section.setSemester(sem);
        section.setDefaultRoom(defaultRoom);

        return mapToDTO(sectionRepository.save(section));
    }

    public void deleteSection(Long id) {
        Section section = findEntityById(id);
        sectionRepository.delete(section);
    }

    // -------------------------------------------------------------------------
    // Package-visible helper — used by CourseOfferingService, TimetableSlotService, etc.
    // -------------------------------------------------------------------------

    public Section findEntityById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with ID: " + id));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Room resolveOptionalRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
    }

    public SectionResponseDTO mapToDTO(Section s) {
        return new SectionResponseDTO(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getStudentCount(),
                s.getDepartment().getId(),
                s.getDepartment().getCode(),
                s.getAcademicYear().getId(),
                s.getAcademicYear().getYearName(),
                s.getSemester().getId(),
                s.getSemester().getCode(),
                s.getDefaultRoom() != null ? s.getDefaultRoom().getId() : null,
                s.getDefaultRoom() != null ? s.getDefaultRoom().getRoomNumber() : null
        );
    }
}
