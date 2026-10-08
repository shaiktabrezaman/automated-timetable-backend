package com.timetable.backend.service;

import com.timetable.backend.dto.request.SlotAssignRequestDTO;
import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.exception.SchedulingConflictException;
import com.timetable.backend.exception.ValidationException;
import com.timetable.backend.model.CourseOffering;
import com.timetable.backend.model.Faculty;
import com.timetable.backend.model.Room;
import com.timetable.backend.model.Section;
import com.timetable.backend.model.Subject;
import com.timetable.backend.model.TimetableSlot;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.CourseOfferingRepository;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.TimetableSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Deterministic constraint-based timetable generator service.
 * Schedules all active course offerings for a section using the
 * Most-Constrained-First heuristic and recursive backtracking.
 */
@Service
@Transactional
public class SchedulerService {

    /** Working days: Monday through Saturday */
    private static final List<DayOfWeek> WORKING_DAYS = List.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
    );

    /** Period boundaries */
    private static final int MIN_PERIOD = 1;
    private static final int MAX_PERIOD = 7;
    private static final int LAST_MORNING_PERIOD = 4;
    private static final int FIRST_AFTERNOON_PERIOD = 5;
    private static final int TOTAL_WEEKLY_PERIODS = WORKING_DAYS.size() * MAX_PERIOD; // 6 * 7 = 42

    private final SectionService sectionService;
    private final CourseOfferingRepository courseOfferingRepository;
    private final RoomRepository roomRepository;
    private final TimetableSlotRepository slotRepository;
    private final TimetableSlotService timetableSlotService;

    public SchedulerService(SectionService sectionService,
                            CourseOfferingRepository courseOfferingRepository,
                            RoomRepository roomRepository,
                            TimetableSlotRepository slotRepository,
                            TimetableSlotService timetableSlotService) {
        this.sectionService = sectionService;
        this.courseOfferingRepository = courseOfferingRepository;
        this.roomRepository = roomRepository;
        this.slotRepository = slotRepository;
        this.timetableSlotService = timetableSlotService;
    }

    /**
     * Automatically generates and persists a complete timetable for the given section.
     *
     * @param sectionId The target section ID.
     * @return List of generated timetable slots ordered by day and period.
     * @throws ValidationException if section has no active offerings or invalid period configs.
     * @throws SchedulingConflictException if constraints cannot be satisfied.
     */
    @Transactional
    public List<TimetableSlotResponseDTO> generateTimetable(Long sectionId) {
        // 1. Load and validate target Section
        Section section = sectionService.findEntityById(sectionId);

        // 2. Load active CourseOfferings for this section
        List<CourseOffering> offerings = courseOfferingRepository.findBySectionIdAndActiveTrue(sectionId);
        if (offerings.isEmpty()) {
            throw new ValidationException("No active course offerings found for section '" + section.getCode() + "'.");
        }

        // 3. Validate total weekly periods requirement
        int totalRequiredPeriods = offerings.stream().mapToInt(CourseOffering::getWeeklyPeriods).sum();
        if (totalRequiredPeriods > TOTAL_WEEKLY_PERIODS) {
            throw new SchedulingConflictException(
                    "Total required periods (" + totalRequiredPeriods + ") for section '" + section.getCode() +
                    "' exceeds total available periods in a week (" + TOTAL_WEEKLY_PERIODS + ").");
        }

        // 4. Sort CourseOfferings deterministically using Most-Constrained-First:
        //    Higher duration periods first (e.g. 3-hour Labs before 2-hour Labs before 1-hour Theory),
        //    then higher weekly periods, then subject name and id for stable ordering.
        List<CourseOffering> sortedOfferings = new ArrayList<>(offerings);
        sortedOfferings.sort(Comparator
                .comparing(CourseOffering::getDurationPeriods, Comparator.reverseOrder())
                .thenComparing(CourseOffering::getWeeklyPeriods, Comparator.reverseOrder())
                .thenComparing((CourseOffering co) -> co.getSubject().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(co -> co.getSubject().getId()));

        // 5. Unroll offerings into individual scheduling sessions
        List<SchedulingSession> sessions = new ArrayList<>();
        for (CourseOffering offering : sortedOfferings) {
            int duration = offering.getDurationPeriods();
            int weekly = offering.getWeeklyPeriods();
            if (duration <= 0 || weekly % duration != 0) {
                throw new ValidationException(
                        "Invalid period configuration for subject '" + offering.getSubject().getName() +
                        "': weeklyPeriods (" + weekly + ") must be divisible by durationPeriods (" + duration + ").");
            }
            int numberOfSessions = weekly / duration;
            for (int sessionIdx = 1; sessionIdx <= numberOfSessions; sessionIdx++) {
                sessions.add(new SchedulingSession(offering, sessionIdx, duration));
            }
        }

        // 6. Pre-calculate candidate rooms for each offering
        List<Room> allRooms = roomRepository.findAll();
        Map<Long, List<Room>> offeringRoomsMap = new HashMap<>();
        for (CourseOffering offering : sortedOfferings) {
            List<Room> candidateRooms = determineCandidateRooms(offering, section, allRooms);
            if (candidateRooms.isEmpty()) {
                throw new SchedulingConflictException(
                        "Cannot schedule subject '" + offering.getSubject().getName() +
                        "': no compatible room with capacity >= " + section.getStudentCount() +
                        " exists for type " + offering.getSubject().getType() + ".");
            }
            offeringRoomsMap.put(offering.getId(), candidateRooms);
        }

        // 7. Load existing timetable slots from other sections to avoid faculty and room clashes
        List<TimetableSlot> existingSlots = slotRepository.findAll();
        Set<String> occupiedClashKeys = new HashSet<>();
        for (TimetableSlot slot : existingSlots) {
            // Ignore current section's existing slots because they will be replaced
            if (slot.getSection().getId().equals(section.getId())) {
                continue;
            }
            DayOfWeek day = slot.getDay();
            int period = slot.getPeriodIndex();
            if (slot.getFaculty() != null) {
                occupiedClashKeys.add(facultyKey(slot.getFaculty().getId(), day, period));
            }
            if (slot.getRoom() != null) {
                occupiedClashKeys.add(roomKey(slot.getRoom().getId(), day, period));
            }
        }

        // 8. Execute backtracking search
        List<ScheduledAssignment> assignments = new ArrayList<>();
        BacktrackContext context = new BacktrackContext();

        boolean solved = solve(0, sessions, occupiedClashKeys, assignments, section, offeringRoomsMap, context);
        if (!solved) {
            SchedulingSession failed = context.deepestFailedSession != null ? context.deepestFailedSession : sessions.get(0);
            throw new SchedulingConflictException(
                    "Unable to generate timetable for section '" + section.getCode() +
                    "'. Could not schedule session " + failed.sessionNumber + " of subject '" +
                    failed.offering.getSubject().getName() + "' (" + failed.durationPeriods +
                    " periods) without conflicts for room, faculty, or lunch constraints.");
        }

        // 9. Clear existing slots for this section only
        timetableSlotService.clearSectionTimetable(section.getId());

        // 10. Persist newly scheduled sessions using TimetableSlotService
        List<TimetableSlotResponseDTO> generatedSlots = new ArrayList<>();
        for (ScheduledAssignment assignment : assignments) {
            SlotAssignRequestDTO dto = new SlotAssignRequestDTO(
                    section.getId(),
                    assignment.day,
                    assignment.startPeriod,
                    assignment.session.durationPeriods,
                    assignment.session.offering.getSubject().getId(),
                    assignment.session.offering.getAssignedFaculty() != null
                            ? assignment.session.offering.getAssignedFaculty().getId() : null,
                    assignment.room.getId()
            );
            List<TimetableSlotResponseDTO> savedRows = timetableSlotService.assignSlot(dto);
            generatedSlots.addAll(savedRows);
        }

        // 11. Sort by Day and Period for consistent, clear response presentation
        generatedSlots.sort(Comparator
                .comparing(TimetableSlotResponseDTO::getDay)
                .thenComparing(TimetableSlotResponseDTO::getPeriodIndex));

        return generatedSlots;
    }

    /**
     * Recursive backtracking search to find a conflict-free schedule for all sessions.
     */
    private boolean solve(int sessionIndex,
                          List<SchedulingSession> sessions,
                          Set<String> occupied,
                          List<ScheduledAssignment> assignments,
                          Section section,
                          Map<Long, List<Room>> offeringRoomsMap,
                          BacktrackContext context) {
        if (sessionIndex >= sessions.size()) {
            return true; // All sessions successfully placed!
        }

        SchedulingSession session = sessions.get(sessionIndex);
        if (sessionIndex > context.maxDepthReached) {
            context.maxDepthReached = sessionIndex;
            context.deepestFailedSession = session;
        }

        CourseOffering offering = session.offering;
        Faculty faculty = offering.getAssignedFaculty();
        int duration = session.durationPeriods;
        List<Integer> validStarts = getValidStartPeriods(duration);
        List<Room> candidateRooms = offeringRoomsMap.get(offering.getId());

        // Deterministic iteration: Days Monday->Saturday, Start periods 1->7, Candidate rooms
        for (DayOfWeek day : WORKING_DAYS) {
            for (int startP : validStarts) {
                // Quick check: does the section have any clash on these consecutive periods?
                if (isSectionClashing(section.getId(), day, startP, duration, occupied)) {
                    continue;
                }
                // Quick check: does faculty have any clash on these consecutive periods?
                if (faculty != null && isFacultyClashing(faculty.getId(), day, startP, duration, occupied)) {
                    continue;
                }

                for (Room room : candidateRooms) {
                    if (isRoomClashing(room.getId(), day, startP, duration, occupied)) {
                        continue;
                    }

                    // Place session
                    occupy(section.getId(), faculty, room.getId(), day, startP, duration, occupied);
                    ScheduledAssignment assignment = new ScheduledAssignment(session, day, startP, room);
                    assignments.add(assignment);

                    // Recurse to next session
                    if (solve(sessionIndex + 1, sessions, occupied, assignments, section, offeringRoomsMap, context)) {
                        return true;
                    }

                    // Backtrack / Undo placement
                    assignments.remove(assignments.size() - 1);
                    release(section.getId(), faculty, room.getId(), day, startP, duration, occupied);
                }
            }
        }

        return false;
    }

    /**
     * Determines candidate rooms matching capacity and subject type,
     * ordered deterministically with preferredRoom first.
     */
    private List<Room> determineCandidateRooms(CourseOffering offering, Section section, List<Room> allRooms) {
        Subject subject = offering.getSubject();
        boolean isLab = (subject.getType() == SubjectType.LAB);
        int requiredCapacity = section.getStudentCount();

        // Filter by capacity and LAB/non-LAB compatibility
        List<Room> validRooms = allRooms.stream()
                .filter(r -> r.getCapacity() >= requiredCapacity)
                .filter(r -> isLab ? (r.getType() == RoomType.LAB) : (r.getType() != RoomType.LAB))
                .sorted(Comparator.comparing(Room::getRoomNumber, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Room> orderedRooms = new ArrayList<>();

        // 1. Preferred room from CourseOffering has highest priority
        Room preferred = offering.getPreferredRoom();
        if (preferred != null && validRooms.contains(preferred)) {
            orderedRooms.add(preferred);
        }

        // 2. Section defaultRoom has second priority (for non-lab subjects)
        Room defaultRoom = section.getDefaultRoom();
        if (defaultRoom != null && !isLab && validRooms.contains(defaultRoom) && !orderedRooms.contains(defaultRoom)) {
            orderedRooms.add(defaultRoom);
        }

        // 3. Prefer standard CLASSROOM over SEMINAR_HALL for non-lab subjects
        if (!isLab) {
            for (Room r : validRooms) {
                if (r.getType() == RoomType.CLASSROOM && !orderedRooms.contains(r)) {
                    orderedRooms.add(r);
                }
            }
        }

        // 4. Add any remaining valid rooms
        for (Room r : validRooms) {
            if (!orderedRooms.contains(r)) {
                orderedRooms.add(r);
            }
        }

        return orderedRooms;
    }

    /**
     * Calculates valid starting periods for a given duration.
     * Ensures periodIndex + duration - 1 <= 7 and does not cross the lunch break (between P4 and P5).
     */
    private List<Integer> getValidStartPeriods(int durationPeriods) {
        List<Integer> starts = new ArrayList<>();
        for (int p = MIN_PERIOD; p <= MAX_PERIOD - durationPeriods + 1; p++) {
            int end = p + durationPeriods - 1;
            boolean crossesLunch = (p <= LAST_MORNING_PERIOD && end >= FIRST_AFTERNOON_PERIOD);
            if (!crossesLunch) {
                starts.add(p);
            }
        }
        return starts;
    }

    // Clash detection helpers
    private boolean isSectionClashing(Long sectionId, DayOfWeek day, int startP, int duration, Set<String> occupied) {
        for (int i = 0; i < duration; i++) {
            if (occupied.contains(sectionKey(sectionId, day, startP + i))) return true;
        }
        return false;
    }

    private boolean isFacultyClashing(Long facultyId, DayOfWeek day, int startP, int duration, Set<String> occupied) {
        for (int i = 0; i < duration; i++) {
            if (occupied.contains(facultyKey(facultyId, day, startP + i))) return true;
        }
        return false;
    }

    private boolean isRoomClashing(Long roomId, DayOfWeek day, int startP, int duration, Set<String> occupied) {
        for (int i = 0; i < duration; i++) {
            if (occupied.contains(roomKey(roomId, day, startP + i))) return true;
        }
        return false;
    }

    private void occupy(Long sectionId, Faculty faculty, Long roomId, DayOfWeek day, int startP, int duration, Set<String> occupied) {
        for (int i = 0; i < duration; i++) {
            int p = startP + i;
            occupied.add(sectionKey(sectionId, day, p));
            if (faculty != null) occupied.add(facultyKey(faculty.getId(), day, p));
            occupied.add(roomKey(roomId, day, p));
        }
    }

    private void release(Long sectionId, Faculty faculty, Long roomId, DayOfWeek day, int startP, int duration, Set<String> occupied) {
        for (int i = 0; i < duration; i++) {
            int p = startP + i;
            occupied.remove(sectionKey(sectionId, day, p));
            if (faculty != null) occupied.remove(facultyKey(faculty.getId(), day, p));
            occupied.remove(roomKey(roomId, day, p));
        }
    }

    private String sectionKey(Long sectionId, DayOfWeek day, int period) {
        return "SEC:" + sectionId + ":" + day + ":" + period;
    }

    private String facultyKey(Long facultyId, DayOfWeek day, int period) {
        return "FAC:" + facultyId + ":" + day + ":" + period;
    }

    private String roomKey(Long roomId, DayOfWeek day, int period) {
        return "ROOM:" + roomId + ":" + day + ":" + period;
    }

    // Inner data models for scheduling
    private static class SchedulingSession {
        final CourseOffering offering;
        final int sessionNumber;
        final int durationPeriods;

        SchedulingSession(CourseOffering offering, int sessionNumber, int durationPeriods) {
            this.offering = offering;
            this.sessionNumber = sessionNumber;
            this.durationPeriods = durationPeriods;
        }
    }

    private static class ScheduledAssignment {
        final SchedulingSession session;
        final DayOfWeek day;
        final int startPeriod;
        final Room room;

        ScheduledAssignment(SchedulingSession session, DayOfWeek day, int startPeriod, Room room) {
            this.session = session;
            this.day = day;
            this.startPeriod = startPeriod;
            this.room = room;
        }
    }

    private static class BacktrackContext {
        int maxDepthReached = -1;
        SchedulingSession deepestFailedSession = null;
    }
}
