package com.timetable.backend.service;

import com.timetable.backend.dto.request.CourseOfferingRequestDTO;
import com.timetable.backend.dto.response.CourseOfferingResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.CourseOffering;
import com.timetable.backend.model.Faculty;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.Subject;
import com.timetable.backend.repository.CourseOfferingRepository;
import com.timetable.backend.repository.FacultyRepository;
import com.timetable.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseOfferingService {

    private final CourseOfferingRepository courseOfferingRepository;
    private final SectionService sectionService;
    private final SubjectService subjectService;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;

    public CourseOfferingService(CourseOfferingRepository courseOfferingRepository,
                                 SectionService sectionService,
                                 SubjectService subjectService,
                                 FacultyRepository facultyRepository,
                                 RoomRepository roomRepository) {
        this.courseOfferingRepository = courseOfferingRepository;
        this.sectionService = sectionService;
        this.subjectService = subjectService;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CourseOfferingResponseDTO> getAllCourseOfferings(Long sectionId, Long subjectId, Long facultyId) {
        List<CourseOffering> offerings;
        if (sectionId != null) {
            offerings = courseOfferingRepository.findBySectionId(sectionId);
        } else if (subjectId != null) {
            offerings = courseOfferingRepository.findBySubjectId(subjectId);
        } else if (facultyId != null) {
            offerings = courseOfferingRepository.findByAssignedFacultyId(facultyId);
        } else {
            offerings = courseOfferingRepository.findAll();
        }
        return offerings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseOfferingResponseDTO> getActiveOfferingsBySection(Long sectionId) {
        sectionService.findEntityById(sectionId); // validates section exists
        return courseOfferingRepository.findBySectionIdAndActiveTrue(sectionId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseOfferingResponseDTO getCourseOfferingById(Long id) {
        return mapToDTO(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    public CourseOfferingResponseDTO createCourseOffering(CourseOfferingRequestDTO dto) {
        validatePeriods(dto.getWeeklyPeriods(), dto.getDurationPeriods());

        Section section   = sectionService.findEntityById(dto.getSectionId());
        Subject subject   = subjectService.findEntityById(dto.getSubjectId());
        Faculty faculty   = resolveOptionalFaculty(dto.getAssignedFacultyId());
        Room preferredRoom = resolveOptionalRoom(dto.getPreferredRoomId());

        validateFacultyWorkload(faculty, dto.getWeeklyPeriods(), null);

        if (courseOfferingRepository.existsBySectionIdAndSubjectId(section.getId(), subject.getId())) {
            throw new ValidationException(
                    "A course offering for section '" + section.getCode() +
                    "' and subject '" + subject.getCode() + "' already exists.");
        }

        CourseOffering offering = new CourseOffering(
                section, subject,
                dto.getWeeklyPeriods(), dto.getDurationPeriods(),
                faculty, preferredRoom,
                dto.isActive());

        return mapToDTO(courseOfferingRepository.save(offering));
    }

    public CourseOfferingResponseDTO updateCourseOffering(Long id, CourseOfferingRequestDTO dto) {
        validatePeriods(dto.getWeeklyPeriods(), dto.getDurationPeriods());

        CourseOffering offering = findEntityById(id);
        Section section         = sectionService.findEntityById(dto.getSectionId());
        Subject subject         = subjectService.findEntityById(dto.getSubjectId());
        Faculty faculty         = resolveOptionalFaculty(dto.getAssignedFacultyId());
        Room preferredRoom      = resolveOptionalRoom(dto.getPreferredRoomId());

        validateFacultyWorkload(faculty, dto.getWeeklyPeriods(), offering.getId());

        // Uniqueness check: reject only if the duplicate belongs to a *different* offering
        courseOfferingRepository.findBySectionIdAndSubjectId(section.getId(), subject.getId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(offering.getId())) {
                        throw new ValidationException(
                                "A course offering for section '" + section.getCode() +
                                "' and subject '" + subject.getCode() + "' already exists.");
                    }
                });

        offering.setSection(section);
        offering.setSubject(subject);
        offering.setWeeklyPeriods(dto.getWeeklyPeriods());
        offering.setDurationPeriods(dto.getDurationPeriods());
        offering.setAssignedFaculty(faculty);
        offering.setPreferredRoom(preferredRoom);
        offering.setActive(dto.isActive());

        return mapToDTO(courseOfferingRepository.save(offering));
    }

    public void deleteCourseOffering(Long id) {
        courseOfferingRepository.delete(findEntityById(id));
    }

    // -------------------------------------------------------------------------
    // Package-visible helper — used by TimetableSlotService
    // -------------------------------------------------------------------------

    public CourseOffering findEntityById(Long id) {
        return courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course Offering not found with ID: " + id));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void validateFacultyWorkload(Faculty faculty, int newWeeklyPeriods, Long currentOfferingId) {
        if (faculty == null) {
            return;
        }
        int existingWorkload = courseOfferingRepository.findByAssignedFacultyId(faculty.getId()).stream()
                .filter(co -> currentOfferingId == null || !co.getId().equals(currentOfferingId))
                .mapToInt(CourseOffering::getWeeklyPeriods)
                .sum();

        int totalWorkload = existingWorkload + newWeeklyPeriods;
        if (totalWorkload > faculty.getMaxWeeklyHours()) {
            throw new ValidationException(
                    "Faculty '" + faculty.getName() + "' weekly workload (" + totalWorkload +
                    ") exceeds maximum allowed hours (" + faculty.getMaxWeeklyHours() + ").");
        }
    }

    private void validatePeriods(int weeklyPeriods, int durationPeriods) {
        if (weeklyPeriods <= 0) {
            throw new ValidationException("weeklyPeriods must be greater than 0.");
        }
        if (durationPeriods <= 0) {
            throw new ValidationException("durationPeriods must be greater than 0.");
        }
        if (weeklyPeriods < durationPeriods) {
            throw new ValidationException(
                    "weeklyPeriods (" + weeklyPeriods + ") must be >= durationPeriods (" + durationPeriods + ").");
        }
        if (weeklyPeriods % durationPeriods != 0) {
            throw new ValidationException(
                    "weeklyPeriods (" + weeklyPeriods + ") must be divisible by durationPeriods (" + durationPeriods + ").");
        }
    }

    private Faculty resolveOptionalFaculty(Long facultyId) {
        if (facultyId == null) return null;
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found with ID: " + facultyId));
    }

    private Room resolveOptionalRoom(Long roomId) {
        if (roomId == null) return null;
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));
    }

    public CourseOfferingResponseDTO mapToDTO(CourseOffering co) {
        return new CourseOfferingResponseDTO(
                co.getId(),
                co.getSection().getId(),
                co.getSection().getCode(),
                co.getSubject().getId(),
                co.getSubject().getCode(),
                co.getSubject().getName(),
                co.getSubject().getShortName(),
                co.getSubject().getType(),
                co.getWeeklyPeriods(),
                co.getDurationPeriods(),
                co.getAssignedFaculty() != null ? co.getAssignedFaculty().getId()   : null,
                co.getAssignedFaculty() != null ? co.getAssignedFaculty().getName()  : null,
                co.getPreferredRoom()   != null ? co.getPreferredRoom().getId()      : null,
                co.getPreferredRoom()   != null ? co.getPreferredRoom().getRoomNumber() : null,
                co.isActive()
        );
    }
}
