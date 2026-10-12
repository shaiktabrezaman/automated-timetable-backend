package com.timetable.backend.service;

import com.timetable.backend.dto.request.SlotAssignRequestDTO;
import com.timetable.backend.dto.response.TimetableGridResponseDTO;
import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.exception.ResourceNotFoundException;
import com.timetable.backend.exception.SchedulingConflictException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.Department;
import com.timetable.backend.model.Faculty;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.Semester;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.TimetableSlot;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.FacultyRepository;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.SubjectRepository;
import com.timetable.backend.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimetableSlotService {

    /** Lunch break boundary: periods 1–4 are morning, 5–7 are afternoon. */
    private static final int LAST_MORNING_PERIOD  = 4;
    private static final int FIRST_AFTERNOON_PERIOD = 5;
    private static final int MAX_PERIOD = 7;
    private static final int MIN_PERIOD = 1;

    public static class ActivitySpec {
        private final String code;
        private final String name;
        private final String shortName;
        private final int weeklyPeriods;

        public ActivitySpec(String code, String name, String shortName, int weeklyPeriods) {
            this.code = code;
            this.name = name;
            this.shortName = shortName;
            this.weeklyPeriods = weeklyPeriods;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getShortName() { return shortName; }
        public int getWeeklyPeriods() { return weeklyPeriods; }
    }

    public static final List<ActivitySpec> INSTITUTIONAL_ACTIVITIES = List.of(
            new ActivitySpec("ACT-LIB", "Library", "LIB", 3),
            new ActivitySpec("ACT-NPT", "NPTEL Self-Study", "NPTEL", 3),
            new ActivitySpec("ACT-SPT", "Sports & Games", "SPORTS", 2),
            new ActivitySpec("ACT-CNS", "Counselling / Mentoring", "COUNS", 2),
            new ActivitySpec("ACT-NSS", "NSS / NCC / Social Activity", "NSS/NCC", 2)
    );

    private static final List<String> ACTIVITY_SCHEDULE = List.of(
            "ACT-LIB", "ACT-SPT",
            "ACT-NPT", "ACT-CNS",
            "ACT-LIB", "ACT-NSS",
            "ACT-NPT", "ACT-SPT",
            "ACT-LIB", "ACT-CNS",
            "ACT-NPT", "ACT-NSS"
    );

    private static final List<DayOfWeek> WORKING_DAYS = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    public static class DayPeriodCoordinate {
        private final DayOfWeek day;
        private final int periodIndex;

        public DayPeriodCoordinate(DayOfWeek day, int periodIndex) {
            this.day = day;
            this.periodIndex = periodIndex;
        }

        public DayOfWeek getDay() { return day; }
        public int getPeriodIndex() { return periodIndex; }
    }

    private final TimetableSlotRepository slotRepository;
    private final SectionService sectionService;
    private final SubjectService subjectService;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final CourseOfferingService courseOfferingService;
    private final SubjectRepository subjectRepository;

    public TimetableSlotService(TimetableSlotRepository slotRepository,
                                SectionService sectionService,
                                SubjectService subjectService,
                                FacultyRepository facultyRepository,
                                RoomRepository roomRepository,
                                CourseOfferingService courseOfferingService,
                                SubjectRepository subjectRepository) {
        this.slotRepository       = slotRepository;
        this.sectionService       = sectionService;
        this.subjectService       = subjectService;
        this.facultyRepository    = facultyRepository;
        this.roomRepository       = roomRepository;
        this.courseOfferingService = courseOfferingService;
        this.subjectRepository    = subjectRepository;
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<TimetableSlotResponseDTO> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

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

    /**
     * Fills all 12 empty periods for a section with 5 institutional activities:
     * - ACT-LIB (Library): 3 weekly periods
     * - ACT-NPT (NPTEL Self-Study): 3 weekly periods
     * - ACT-SPT (Sports & Games): 2 weekly periods
     * - ACT-CNS (Counselling / Mentoring): 2 weekly periods
     * - ACT-NSS (NSS / NCC / Social Activity): 2 weekly periods
     * Total: 12 periods.
     * All activities have faculty = null, room = null, and spanPeriods = 1.
     * Operation is transactional and idempotent.
     */
    @Transactional
    public List<TimetableSlotResponseDTO> fillActivities(Long sectionId) {
        Section section = sectionService.findEntityById(sectionId);
        List<TimetableSlot> currentSlots = slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(sectionId);

        Set<String> institutionalCodes = INSTITUTIONAL_ACTIVITIES.stream()
                .map(ActivitySpec::getCode)
                .collect(Collectors.toSet());

        // Check if section is already filled (idempotent check)
        if (currentSlots.size() == 42) {
            long activityCount = currentSlots.stream()
                    .filter(s -> s.getSubject() != null && institutionalCodes.contains(s.getSubject().getCode()))
                    .count();
            if (activityCount == 12) {
                // Already complete, return existing slots
                return currentSlots.stream().map(this::mapToDTO).collect(Collectors.toList());
            } else {
                throw new ValidationException(
                        "Section '" + section.getCode() + "' has 42 slots, but does not have the expected 12 activity slots."
                );
            }
        }

        // Map occupied coordinates
        Set<String> occupiedCoords = new HashSet<>();
        for (TimetableSlot slot : currentSlots) {
            occupiedCoords.add(slot.getDay() + ":" + slot.getPeriodIndex());
        }

        // Find empty coordinates across Mon-Sat, periods 1-7
        List<DayPeriodCoordinate> emptyCoords = new ArrayList<>();
        for (DayOfWeek day : WORKING_DAYS) {
            for (int p = MIN_PERIOD; p <= MAX_PERIOD; p++) {
                if (!occupiedCoords.contains(day + ":" + p)) {
                    emptyCoords.add(new DayPeriodCoordinate(day, p));
                }
            }
        }

        // Validate exactly 12 empty periods are available
        if (emptyCoords.size() != 12) {
            throw new ValidationException(
                    "Section '" + section.getCode() + "' has " + emptyCoords.size() +
                    " empty periods (expected 12 empty periods from 30 scheduled academic slots)."
            );
        }

        // Snapshot original academic slot IDs to verify preservation
        Set<Long> originalSlotIds = currentSlots.stream()
                .map(TimetableSlot::getId)
                .collect(Collectors.toSet());

        // Ensure activity subjects exist
        Map<String, Subject> activitySubjectMap = ensureActivitySubjectsExist(section.getDepartment(), section.getSemester());

        // Create 12 activity slots deterministically
        List<TimetableSlot> slotsToSave = new ArrayList<>();
        for (int i = 0; i < emptyCoords.size(); i++) {
            DayPeriodCoordinate coord = emptyCoords.get(i);
            String activityCode = ACTIVITY_SCHEDULE.get(i);
            Subject subject = activitySubjectMap.get(activityCode);

            // Double check no conflict exists
            if (slotRepository.existsBySectionIdAndDayAndPeriodIndex(section.getId(), coord.getDay(), coord.getPeriodIndex())) {
                throw new SchedulingConflictException(
                        "Section already has a slot on " + coord.getDay() + " period " + coord.getPeriodIndex() + "."
                );
            }

            TimetableSlot activitySlot = new TimetableSlot(
                    section,
                    coord.getDay(),
                    coord.getPeriodIndex(),
                    1,     // spanPeriods = 1
                    false, // isMultiPeriodHead = false
                    false, // isMultiPeriodContinuation = false
                    subject,
                    null,  // faculty = null
                    null   // room = null
            );
            slotsToSave.add(activitySlot);
        }

        slotRepository.saveAll(slotsToSave);

        // Verification after save
        List<TimetableSlot> allSectionSlots = slotRepository.findBySectionIdOrderByDayAscPeriodIndexAsc(sectionId);
        if (allSectionSlots.size() != 42) {
            throw new ValidationException("Expected 42 total slots after filling activities, but found " + allSectionSlots.size());
        }

        // Verify all original academic slot IDs are preserved
        for (Long originalId : originalSlotIds) {
            boolean exists = allSectionSlots.stream().anyMatch(s -> Objects.equals(s.getId(), originalId));
            if (!exists) {
                throw new ValidationException("Academic slot ID " + originalId + " was lost during activity fill.");
            }
        }

        // Verify weekly activity frequencies
        Map<String, Long> activityCounts = allSectionSlots.stream()
                .filter(s -> s.getSubject() != null && institutionalCodes.contains(s.getSubject().getCode()))
                .collect(Collectors.groupingBy(s -> s.getSubject().getCode(), Collectors.counting()));

        for (ActivitySpec spec : INSTITUTIONAL_ACTIVITIES) {
            long actual = activityCounts.getOrDefault(spec.getCode(), 0L);
            if (actual != spec.getWeeklyPeriods()) {
                throw new ValidationException(
                        "Activity " + spec.getCode() + " expected frequency " + spec.getWeeklyPeriods() + " but got " + actual
                );
            }
        }

        // Verify null faculty and room on all institutional activity slots
        for (TimetableSlot s : allSectionSlots) {
            if (s.getSubject() != null && institutionalCodes.contains(s.getSubject().getCode())) {
                if (s.getFaculty() != null || s.getRoom() != null) {
                    throw new ValidationException("Activity slot has non-null faculty or room.");
                }
            }
        }

        return allSectionSlots.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Map<String, Subject> ensureActivitySubjectsExist(Department department, Semester semester) {
        Map<String, Subject> map = new HashMap<>();
        for (ActivitySpec spec : INSTITUTIONAL_ACTIVITIES) {
            Subject subject = subjectRepository.findByCode(spec.getCode()).orElseGet(() -> {
                Subject newSubj = new Subject(
                        spec.getCode(),
                        spec.getName(),
                        spec.getShortName(),
                        SubjectType.ACTIVITY,
                        department,
                        semester,
                        null
                );
                return subjectRepository.save(newSubj);
            });
            map.put(spec.getCode(), subject);
        }
        return map;
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
        String reason = RoomCompatibilityValidator.getIncompatibilityReason(room, subject);
        if (reason != null) {
            throw new SchedulingConflictException(reason);
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
