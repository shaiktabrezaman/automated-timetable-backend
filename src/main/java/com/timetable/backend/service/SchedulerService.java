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

        int targetDailyLoad = (int) Math.ceil((double) totalRequiredPeriods / WORKING_DAYS.size());

        boolean solved = solve(0, sessions, occupiedClashKeys, assignments, section, offeringRoomsMap, context, targetDailyLoad);
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
     * Recursive backtracking search to find a conflict-free schedule for all sessions,
     * exploring candidate slots prioritized by quality preferences (subject spread and daily balance).
     */
    private boolean solve(int sessionIndex,
                          List<SchedulingSession> sessions,
                          Set<String> occupied,
                          List<ScheduledAssignment> assignments,
                          Section section,
                          Map<Long, List<Room>> offeringRoomsMap,
                          BacktrackContext context,
                          int targetDailyLoad) {
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

        // Prioritize candidate (day, startPeriod) slots:
        // 1. Spread repeated sessions of the same offering across different days (soft preference).
        // 2. Balance workload across Monday-Saturday towards targetDailyLoad (soft preference).
        // 3. Fall back gracefully to less-preferred slots if clashes occur.
        List<SlotCandidate> candidateSlots = getPrioritizedCandidateSlots(
                session, assignments, validStarts, targetDailyLoad, section.getId(), faculty, occupied);

        for (SlotCandidate slot : candidateSlots) {
            DayOfWeek day = slot.day;
            int startP = slot.startPeriod;

            for (Room room : candidateRooms) {
                if (isRoomClashing(room.getId(), day, startP, duration, occupied)) {
                    continue;
                }

                // Place session
                occupy(section.getId(), faculty, room.getId(), day, startP, duration, occupied);
                ScheduledAssignment assignment = new ScheduledAssignment(session, day, startP, room);
                assignments.add(assignment);

                // Recurse to next session
                if (solve(sessionIndex + 1, sessions, occupied, assignments, section, offeringRoomsMap, context, targetDailyLoad)) {
                    return true;
                }

                // Backtrack / Undo placement
                assignments.remove(assignments.size() - 1);
                release(section.getId(), faculty, room.getId(), day, startP, duration, occupied);
            }
        }

        return false;
    }

    /**
     * Builds and deterministically sorts candidate slot placements (day + startPeriod)
     * using quality preferences while ensuring all valid combinations remain available for backtracking.
     */
    private List<SlotCandidate> getPrioritizedCandidateSlots(
            SchedulingSession session,
            List<ScheduledAssignment> assignments,
            List<Integer> validStarts,
            int targetDailyLoad,
            Long sectionId,
            Faculty faculty,
            Set<String> occupied) {

        Map<DayOfWeek, Integer> dayPeriodsMap = new EnumMap<>(DayOfWeek.class);
        Map<DayOfWeek, Integer> dayMorningPeriodsMap = new EnumMap<>(DayOfWeek.class);
        Map<DayOfWeek, Integer> dayAfternoonPeriodsMap = new EnumMap<>(DayOfWeek.class);
        Map<DayOfWeek, Integer> offeringSessionsMap = new EnumMap<>(DayOfWeek.class);

        for (DayOfWeek day : WORKING_DAYS) {
            dayPeriodsMap.put(day, 0);
            dayMorningPeriodsMap.put(day, 0);
            dayAfternoonPeriodsMap.put(day, 0);
            offeringSessionsMap.put(day, 0);
        }

        int morningBlocks = 0;
        int afternoonBlocks = 0;

        for (ScheduledAssignment a : assignments) {
            int dur = a.session.durationPeriods;
            dayPeriodsMap.put(a.day, dayPeriodsMap.get(a.day) + dur);

            if (a.startPeriod <= LAST_MORNING_PERIOD) {
                dayMorningPeriodsMap.put(a.day, dayMorningPeriodsMap.get(a.day) + dur);
                if (dur >= 2) {
                    morningBlocks++;
                }
            } else {
                dayAfternoonPeriodsMap.put(a.day, dayAfternoonPeriodsMap.get(a.day) + dur);
                if (dur >= 2) {
                    afternoonBlocks++;
                }
            }

            boolean isSameOffering = (session.offering.getId() != null && a.session.offering.getId() != null)
                    ? session.offering.getId().equals(a.session.offering.getId())
                    : (a.session.offering == session.offering
                    || Objects.equals(a.session.offering.getSubject().getCode(), session.offering.getSubject().getCode()));
            if (isSameOffering) {
                offeringSessionsMap.put(a.day, offeringSessionsMap.get(a.day) + 1);
            }
        }

        final int mBlocks = morningBlocks;
        final int aBlocks = afternoonBlocks;
        int duration = session.durationPeriods;
        List<SlotCandidate> candidates = new ArrayList<>();

        for (DayOfWeek day : WORKING_DAYS) {
            for (int startP : validStarts) {
                // Quick filter: skip slots where the section or faculty already has a hard conflict
                if (isSectionClashing(sectionId, day, startP, duration, occupied)) {
                    continue;
                }
                if (faculty != null && isFacultyClashing(faculty.getId(), day, startP, duration, occupied)) {
                    continue;
                }
                candidates.add(new SlotCandidate(day, startP));
            }
        }

        candidates.sort((c1, c2) -> {
            // 1. Offering spread preference: prefer days where this offering has fewer sessions today
            int offCount1 = offeringSessionsMap.get(c1.day);
            int offCount2 = offeringSessionsMap.get(c2.day);
            if (offCount1 != offCount2) {
                return Integer.compare(offCount1, offCount2);
            }

            // 2. Workload balance preference: prefer slots that do not exceed targetDailyLoad
            int load1 = dayPeriodsMap.get(c1.day);
            int load2 = dayPeriodsMap.get(c2.day);
            boolean exceeds1 = (load1 + duration > targetDailyLoad);
            boolean exceeds2 = (load2 + duration > targetDailyLoad);
            if (exceeds1 != exceeds2) {
                return Boolean.compare(exceeds1, exceeds2); // false (within budget) comes before true
            }

            // 3. Workload balance preference: prefer days with lower current workload
            if (load1 != load2) {
                return Integer.compare(load1, load2);
            }

            // 4. Deterministic day ordering (Monday -> Saturday)
            if (c1.day != c2.day) {
                return c1.day.compareTo(c2.day);
            }

            // 5. Intra-day session balancing:
            // For multi-period blocks (labs/activities): balance between morning (P1-P3) and afternoon (P5-P7) blocks.
            // For theory: balance morning vs afternoon sessions on that day so later periods (P6/P7) are naturally utilized.
            if (duration >= 2) {
                boolean isAft1 = (c1.startPeriod >= FIRST_AFTERNOON_PERIOD);
                boolean isAft2 = (c2.startPeriod >= FIRST_AFTERNOON_PERIOD);
                boolean preferAfternoonBlock = (mBlocks > aBlocks);
                boolean match1 = (isAft1 == preferAfternoonBlock);
                boolean match2 = (isAft2 == preferAfternoonBlock);
                if (match1 != match2) {
                    return Boolean.compare(!match1, !match2); // true (preferred session) comes before false
                }
            } else {
                int targetM = (int) Math.round(targetDailyLoad * (double) LAST_MORNING_PERIOD / MAX_PERIOD);
                int targetA = Math.max(1, targetDailyLoad - targetM);

                boolean isAft1 = (c1.startPeriod >= FIRST_AFTERNOON_PERIOD);
                boolean isAft2 = (c2.startPeriod >= FIRST_AFTERNOON_PERIOD);

                int mLoad = dayMorningPeriodsMap.get(c1.day);
                int aLoad = dayAfternoonPeriodsMap.get(c1.day);

                boolean sessExceeds1 = isAft1
                        ? (aLoad + duration > targetA && mLoad < targetM)
                        : (mLoad + duration > targetM && aLoad < targetA);
                boolean sessExceeds2 = isAft2
                        ? (aLoad + duration > targetA && mLoad < targetM)
                        : (mLoad + duration > targetM && aLoad < targetA);

                if (sessExceeds1 != sessExceeds2) {
                    return Boolean.compare(sessExceeds1, sessExceeds2); // within session budget comes first
                }
            }

            // 6. Within the preferred session, deterministic period index ordering
            return Integer.compare(c1.startPeriod, c2.startPeriod);
        });

        return candidates;
    }

    /**
     * Determines candidate rooms matching capacity and subject type,
     * ordered deterministically with preferredRoom first.
     */
    private List<Room> determineCandidateRooms(CourseOffering offering, Section section, List<Room> allRooms) {
        Subject subject = offering.getSubject();
        int requiredCapacity = section.getStudentCount();

        // Filter by capacity and room compatibility (type, exclusivity, seminar hall)
        List<Room> validRooms = allRooms.stream()
                .filter(r -> r.getCapacity() >= requiredCapacity)
                .filter(r -> RoomCompatibilityValidator.isCompatible(r, subject))
                .sorted(Comparator.comparing(Room::getRoomNumber, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        List<Room> orderedRooms = new ArrayList<>();

        // 1. Preferred room from CourseOffering has highest priority
        Room preferred = offering.getPreferredRoom();
        if (preferred != null && validRooms.contains(preferred)) {
            orderedRooms.add(preferred);
        }

        // 2. Section defaultRoom has second priority (for classroom/theory subjects)
        Room defaultRoom = section.getDefaultRoom();
        if (defaultRoom != null && validRooms.contains(defaultRoom) && !orderedRooms.contains(defaultRoom)) {
            orderedRooms.add(defaultRoom);
        }

        // 3. For theory subjects, prefer standard CLASSROOM over others
        if (subject.getType() == SubjectType.THEORY || subject.getType() == SubjectType.ELECTIVE) {
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

    private static class SlotCandidate {
        final DayOfWeek day;
        final int startPeriod;

        SlotCandidate(DayOfWeek day, int startPeriod) {
            this.day = day;
            this.startPeriod = startPeriod;
        }
    }

    private static class BacktrackContext {
        int maxDepthReached = -1;
        SchedulingSession deepestFailedSession = null;
    }
}
