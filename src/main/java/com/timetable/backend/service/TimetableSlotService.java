package com.timetable.backend.service;

import com.timetable.backend.dto.request.SlotAssignRequestDTO;
import com.timetable.backend.dto.response.TimetableGridResponseDTO;
import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.SchedulingConflictException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.Faculty;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.TimetableSlot;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.FacultyRepository;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableSlotService {

    /** Lunch break boundary: periods 1–4 are morning, 5–7 are afternoon. */
    private static final int LAST_MORNING_PERIOD  = 4;
    private static final int FIRST_AFTERNOON_PERIOD = 5;
    private static final int MAX_PERIOD = 7;
    private static final int MIN_PERIOD = 1;

    private final TimetableSlotRepository slotRepository;
    private final SectionService sectionService;
    private final SubjectService subjectService;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final CourseOfferingService courseOfferingService;

    public TimetableSlotService(TimetableSlotRepository slotRepository,
                                SectionService sectionService,
                                SubjectService subjectService,
                                FacultyRepository facultyRepository,
                                RoomRepository roomRepository,
                                CourseOfferingService courseOfferingService) {
        this.slotRepository       = slotRepository;
        this.sectionService       = sectionService;
        this.subjectService       = subjectService;
        this.facultyRepository    = facultyRepository;
        this.roomRepository       = roomRepository;
        this.courseOfferingService = courseOfferingService;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TimetableSlotResponseDTO> getSlotsBySection(Long sectionId) {
        sectionService.findEntityById(sectionId); // validate exists
        return slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(sectionId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotResponseDTO> getSlotsByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty not found with ID: " + facultyId);
        }
        return slotRepository.findByFacultyId(facultyId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotResponseDTO> getSlotsByRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }
        return slotRepository.findByRoomId(roomId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TimetableGridResponseDTO getTimetableGrid(Long sectionId) {
        Section section = sectionService.findEntityById(sectionId);
        List<TimetableSlotResponseDTO> slots = slotRepository
                .findBySectionIdOrderByDayAscPeriodIndexAsc(sectionId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        List<com.timetable.backend.dto.response.CourseOfferingResponseDTO> offerings =
                courseOfferingService.getActiveOfferingsBySection(sectionId);
        return new TimetableGridResponseDTO(sectionService.mapToDTO(section), slots, offerings);
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Assigns one logical session (spanning {@code spanPeriods} consecutive periods)
     * to the timetable.  For a multi-period session, one HEAD row and (spanPeriods-1)
     * CONTINUATION rows are persisted, all sharing the same day and consecutive
     * periodIndex values.
     */
    public List<TimetableSlotResponseDTO> assignSlot(SlotAssignRequestDTO dto) {
        int periodIndex = dto.getPeriodIndex();
        int spanPeriods = dto.getSpanPeriods();
        DayOfWeek day   = dto.getDay();

        // ---- structural validation ----
        validatePeriodRange(periodIndex, spanPeriods);
        validateNoLunchCross(periodIndex, spanPeriods);

        // ---- resolve entities ----
        Section section = sectionService.findEntityById(dto.getSectionId());
        Subject subject = subjectService.findEntityById(dto.getSubjectId());
        Faculty faculty = resolveOptionalFaculty(dto.getFacultyId());
        Room    room    = resolveOptionalRoom(dto.getRoomId());

        // ---- room constraint checks ----
        if (room != null) {
            validateRoomCapacity(room, section);
            validateRoomSubjectTypeCompatibility(room, subject);
        }

        // ---- conflict checks for all periods in the span ----
        for (int offset = 0; offset < spanPeriods; offset++) {
            int p = periodIndex + offset;
            checkSectionConflict(section.getId(), day, p);
            if (faculty != null) checkFacultyConflict(faculty.getId(), day, p);
            if (room    != null) checkRoomConflict(room.getId(), day, p);
        }

        // ---- persist slots ----
        List<TimetableSlot> saved = new ArrayList<>();
        for (int offset = 0; offset < spanPeriods; offset++) {
            boolean isHead = (offset == 0);
            boolean isCont = (offset > 0);
            TimetableSlot slot = new TimetableSlot(
                    section, day, periodIndex + offset,
                    spanPeriods, isHead, isCont,
                    subject, faculty, room);
            saved.add(slotRepository.save(slot));
        }

        return saved.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Deletes a single slot row by ID.
     * If it is the head of a multi-period session, all continuation rows for that
     * session (same section / day / subject, consecutive period indexes) are also
     * deleted to preserve data consistency.
     */
    public void deleteSlot(Long slotId) {
        TimetableSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable slot not found with ID: " + slotId));

        if (slot.isMultiPeriodHead() && slot.getSpanPeriods() > 1) {
            // Delete all continuation rows that belong to this logical session
            int span = slot.getSpanPeriods();
            DayOfWeek day = slot.getDay();
            Long sectionId = slot.getSection().getId();
            for (int offset = 1; offset < span; offset++) {
                slotRepository.findBySectionIdAndDayAndPeriodIndex(
                        sectionId, day, slot.getPeriodIndex() + offset)
                        .ifPresent(slotRepository::delete);
            }
        }
        slotRepository.delete(slot);
    }

    /** Removes every slot row for the given section. */
    public void clearSectionTimetable(Long sectionId) {
        sectionService.findEntityById(sectionId); // validate exists
        slotRepository.deleteBySectionId(sectionId);
    }

    // -------------------------------------------------------------------------
    // Validation helpers
    // -------------------------------------------------------------------------

    private void validatePeriodRange(int periodIndex, int spanPeriods) {
        if (periodIndex < MIN_PERIOD || periodIndex > MAX_PERIOD) {
            throw new ValidationException(
                    "periodIndex must be between " + MIN_PERIOD + " and " + MAX_PERIOD + ".");
        }
        if (spanPeriods < 1 || spanPeriods > 3) {
            throw new ValidationException("spanPeriods must be between 1 and 3.");
        }
        int lastPeriod = periodIndex + spanPeriods - 1;
        if (lastPeriod > MAX_PERIOD) {
            throw new ValidationException(
                    "Slot from period " + periodIndex + " spanning " + spanPeriods +
                    " period(s) exceeds the last period of the day (" + MAX_PERIOD + ").");
        }
    }

    /**
     * A session must not cross the lunch break.
     * Periods 1–4 are morning; periods 5–7 are afternoon.
     * A span starting at or before P4 must end at or before P4.
     */
    private void validateNoLunchCross(int periodIndex, int spanPeriods) {
        int lastPeriod = periodIndex + spanPeriods - 1;
        boolean startsMorning = (periodIndex <= LAST_MORNING_PERIOD);
        boolean endsAfternoon = (lastPeriod >= FIRST_AFTERNOON_PERIOD);
        if (startsMorning && endsAfternoon) {
            throw new ValidationException(
                    "A timetable slot cannot span across the lunch break " +
                    "(periods " + LAST_MORNING_PERIOD + " and " + FIRST_AFTERNOON_PERIOD + " are separated by lunch).");
        }
    }

    private void validateRoomCapacity(Room room, Section section) {
        if (room.getCapacity() < section.getStudentCount()) {
            throw new SchedulingConflictException(
                    "Room '" + room.getRoomNumber() + "' capacity (" + room.getCapacity() +
                    ") is less than section '" + section.getCode() +
                    "' student count (" + section.getStudentCount() + ").");
        }
    }

    private void validateRoomSubjectTypeCompatibility(Room room, Subject subject) {
        boolean subjectIsLab = (subject.getType() == SubjectType.LAB);
        boolean roomIsLab    = (room.getType() == RoomType.LAB);

        if (subjectIsLab && !roomIsLab) {
            throw new SchedulingConflictException(
                    "Subject '" + subject.getName() + "' is a LAB subject and requires a LAB room, " +
                    "but room '" + room.getRoomNumber() + "' is of type " + room.getType() + ".");
        }
        if (!subjectIsLab && roomIsLab) {
            throw new SchedulingConflictException(
                    "Subject '" + subject.getName() + "' is not a LAB subject and cannot be assigned to " +
                    "LAB room '" + room.getRoomNumber() + "'.");
        }
    }

    private void checkSectionConflict(Long sectionId, DayOfWeek day, int period) {
        if (slotRepository.existsBySectionIdAndDayAndPeriodIndex(sectionId, day, period)) {
            throw new SchedulingConflictException(
                    "Section already has a slot on " + day + " period " + period + ".");
        }
    }

    private void checkFacultyConflict(Long facultyId, DayOfWeek day, int period) {
        if (slotRepository.existsByFacultyIdAndDayAndPeriodIndex(facultyId, day, period)) {
            throw new SchedulingConflictException(
                    "Faculty is already assigned to another slot on " + day + " period " + period + ".");
        }
    }

    private void checkRoomConflict(Long roomId, DayOfWeek day, int period) {
        if (slotRepository.existsByRoomIdAndDayAndPeriodIndex(roomId, day, period)) {
            throw new SchedulingConflictException(
                    "Room is already occupied on " + day + " period " + period + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Lookup helpers
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // DTO mapping
    // -------------------------------------------------------------------------

    public TimetableSlotResponseDTO mapToDTO(TimetableSlot slot) {
        return new TimetableSlotResponseDTO(
                slot.getId(),
                slot.getSection().getId(),
                slot.getSection().getCode(),
                slot.getDay(),
                slot.getPeriodIndex(),
                slot.getSpanPeriods(),
                slot.isMultiPeriodHead(),
                slot.isMultiPeriodContinuation(),
                slot.getSubject().getId(),
                slot.getSubject().getCode(),
                slot.getSubject().getName(),
                slot.getSubject().getShortName(),
                slot.getSubject().getType(),
                slot.getFaculty() != null ? slot.getFaculty().getId()   : null,
                slot.getFaculty() != null ? slot.getFaculty().getName() : null,
                slot.getRoom()    != null ? slot.getRoom().getId()       : null,
                slot.getRoom()    != null ? slot.getRoom().getRoomNumber() : null
        );
    }
}
