package com.timetable.backend.service;

import com.timetable.backend.dto.request.SlotAssignRequestDTO;
import com.timetable.backend.dto.response.TimetableSlotResponseDTO;
import com.timetable.backend.model.*;
import com.timetable.backend.model.enums.RoomType;
import com.timetable.backend.model.enums.SubjectType;
import com.timetable.backend.repository.CourseOfferingRepository;
import com.timetable.backend.repository.RoomRepository;
import com.timetable.backend.repository.TimetableSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private SectionService sectionService;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private TimetableSlotRepository slotRepository;

    @Mock
    private TimetableSlotService timetableSlotService;

    private SchedulerService schedulerService;

    // Common test entities
    private Section section;
    private Room defaultClassroom;
    private Room trainingLab;
    private Room seminarHall;
    private Room ordinaryLab1;
    private Room ordinaryLab2;
    private Room ordinaryLab3;

    private Faculty faculty1;
    private Faculty faculty2;
    private Faculty faculty3;
    private Faculty faculty4;
    private Faculty faculty5;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerService(
                sectionService,
                courseOfferingRepository,
                roomRepository,
                slotRepository,
                timetableSlotService
        );

        // Rooms
        defaultClassroom = new Room("E-201", RoomType.CLASSROOM, 70, "E-Block", null);
        defaultClassroom.setId(101L);

        trainingLab = new Room("E-701", RoomType.LAB, 70, "E-Block", null);
        trainingLab.setId(102L);

        seminarHall = new Room("E-202", RoomType.SEMINAR_HALL, 70, "E-Block", null);
        seminarHall.setId(103L);

        ordinaryLab1 = new Room("E-301", RoomType.LAB, 70, "E-Block", null);
        ordinaryLab1.setId(104L);

        ordinaryLab2 = new Room("E-609", RoomType.LAB, 70, "E-Block", null);
        ordinaryLab2.setId(105L);

        ordinaryLab3 = new Room("E-104", RoomType.LAB, 70, "E-Block", null);
        ordinaryLab3.setId(106L);

        // Section
        section = new Section("ECE-A", "II Year ECE Section A", 60, null, null, null, defaultClassroom);
        section.setId(1L);

        // Faculty
        faculty1 = new Faculty("Dr. Smith", "Professor", "smith@college.edu", 18, null);
        faculty1.setId(201L);
        faculty2 = new Faculty("Dr. John", "Assoc Professor", "john@college.edu", 18, null);
        faculty2.setId(202L);
        faculty3 = new Faculty("Dr. Alice", "Asst Professor", "alice@college.edu", 18, null);
        faculty3.setId(203L);
        faculty4 = new Faculty("Dr. Bob", "Professor", "bob@college.edu", 18, null);
        faculty4.setId(204L);
        faculty5 = new Faculty("Dr. Carol", "Professor", "carol@college.edu", 18, null);
        faculty5.setId(205L);

        // Setup mock assignSlot behavior to generate simulated persisted slots
        when(timetableSlotService.assignSlot(any(SlotAssignRequestDTO.class))).thenAnswer(invocation -> {
            SlotAssignRequestDTO dto = invocation.getArgument(0);
            List<TimetableSlotResponseDTO> rows = new ArrayList<>();
            int span = dto.getSpanPeriods();
            for (int i = 0; i < span; i++) {
                int period = dto.getPeriodIndex() + i;
                boolean isHead = (i == 0);
                boolean isCont = (i > 0);
                rows.add(new TimetableSlotResponseDTO(
                        (long) (dto.getDay().getValue() * 100 + period),
                        dto.getSectionId(),
                        section.getCode(),
                        dto.getDay(),
                        period,
                        span,
                        isHead,
                        isCont,
                        dto.getSubjectId(),
                        "SUB-" + dto.getSubjectId(),
                        "Subject " + dto.getSubjectId(),
                        "S" + dto.getSubjectId(),
                        SubjectType.THEORY,
                        dto.getFacultyId(),
                        dto.getFacultyId() != null ? "Faculty " + dto.getFacultyId() : null,
                        dto.getRoomId(),
                        "Room " + dto.getRoomId()
                ));
            }
            return rows;
        });
    }

    private Subject createSubject(Long id, String code, String name, SubjectType type) {
        Subject s = new Subject(code, name, code, type, null, null, null);
        s.setId(id);
        return s;
    }

    private CourseOffering createOffering(Long id, Section sec, Subject sub, int weekly, int duration, Faculty fac, Room prefRoom) {
        CourseOffering co = new CourseOffering(sec, sub, weekly, duration, fac, prefRoom, true);
        co.setId(id);
        return co;
    }

    private List<CourseOffering> buildStandardEceAOfferings() {
        Subject tt = createSubject(1L, "TT2101", "Technical Training", SubjectType.TRAINING);
        Subject edcLab = createSubject(2L, "EMA2110-LAB", "Electronic Devices and Circuits Lab", SubjectType.LAB);
        Subject ssLab = createSubject(3L, "EMA2115-LAB", "Signals and Systems Lab", SubjectType.LAB);
        Subject javaLab = createSubject(4L, "EMA2X19-LAB", "Java Lab", SubjectType.LAB);
        Subject ip1 = createSubject(5L, "EVA2101", "Integrated Project-I", SubjectType.ACTIVITY);

        Subject edc = createSubject(6L, "EMA2110", "Electronic Devices and Circuits", SubjectType.THEORY);
        Subject emtl = createSubject(7L, "EMA2118", "Electromagnetic Theory and Transmission Lines", SubjectType.THEORY);
        Subject ss = createSubject(8L, "EMA2115", "Signals and Systems", SubjectType.THEORY);
        Subject ipts = createSubject(9L, "EMA2116", "Introduction to Probability Theory and Statistics", SubjectType.THEORY);
        Subject javaTheory = createSubject(10L, "EMA2X19", "Java", SubjectType.THEORY);

        return List.of(
                // 5 multi-period sessions (5 * 3 = 15 periods)
                createOffering(101L, section, tt, 3, 3, faculty1, trainingLab),
                createOffering(102L, section, edcLab, 3, 3, faculty2, ordinaryLab1),
                createOffering(103L, section, ssLab, 3, 3, faculty3, ordinaryLab2),
                createOffering(104L, section, javaLab, 3, 3, faculty4, ordinaryLab3),
                createOffering(105L, section, ip1, 3, 3, faculty5, seminarHall),

                // 5 theory offerings with 3 single-period sessions each (5 * 3 = 15 periods)
                createOffering(106L, section, edc, 3, 1, faculty1, defaultClassroom),
                createOffering(107L, section, emtl, 3, 1, faculty2, defaultClassroom),
                createOffering(108L, section, ss, 3, 1, faculty3, defaultClassroom),
                createOffering(109L, section, ipts, 3, 1, faculty4, defaultClassroom),
                createOffering(110L, section, javaTheory, 3, 1, faculty5, defaultClassroom)
        );
    }

    private List<Room> allTestRooms() {
        return List.of(defaultClassroom, trainingLab, seminarHall, ordinaryLab1, ordinaryLab2, ordinaryLab3);
    }

    @Test
    @DisplayName("Requirement 1: Theory offering with 3 weekly single-period sessions is distributed across 3 different days")
    void testTheorySessionsDistributedAcrossDifferentDays() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);
        assertEquals(30, generated.size(), "Should schedule all 30 weekly periods");

        // Inspect EDC Theory (Subject ID 6)
        List<TimetableSlotResponseDTO> edcSlots = generated.stream()
                .filter(s -> Long.valueOf(6L).equals(s.getSubjectId()))
                .collect(Collectors.toList());
        assertEquals(3, edcSlots.size(), "EDC Theory must have exactly 3 periods scheduled");

        Set<DayOfWeek> edcDays = edcSlots.stream().map(TimetableSlotResponseDTO::getDay).collect(Collectors.toSet());
        assertEquals(3, edcDays.size(), "EDC Theory should normally appear on 3 distinct days: " + edcDays);

        // Also verify all other theory subjects (IDs 7, 8, 9, 10) are on 3 distinct days
        for (Long subjectId : List.of(7L, 8L, 9L, 10L)) {
            Set<DayOfWeek> days = generated.stream()
                    .filter(s -> subjectId.equals(s.getSubjectId()))
                    .map(TimetableSlotResponseDTO::getDay)
                    .collect(Collectors.toSet());
            assertEquals(3, days.size(), "Theory Subject " + subjectId + " must be distributed across 3 distinct days");
        }
    }

    @Test
    @DisplayName("Requirement 2: Workload balanced evenly across Monday through Saturday (~5 periods/day)")
    void testWorkloadBalancedAcrossMondayThroughSaturday() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);

        Map<DayOfWeek, Long> periodsPerDay = generated.stream()
                .collect(Collectors.groupingBy(TimetableSlotResponseDTO::getDay, Collectors.counting()));

        // All 6 working days must be utilized
        assertEquals(6, periodsPerDay.size(), "All 6 working days (Mon-Sat) must have scheduled periods");

        // For 30 periods across 6 days, each day should have exactly 5 periods
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)) {
            Long count = periodsPerDay.getOrDefault(day, 0L);
            assertEquals(5L, count, "Day " + day + " should have exactly 5 scheduled periods for 30 total periods");
        }
    }

    @Test
    @DisplayName("Requirement 3: Three-period labs remain consecutive")
    void testThreePeriodLabsRemainConsecutive() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);

        // Check EDC Lab (Subject ID 2, duration 3)
        List<TimetableSlotResponseDTO> edcLabSlots = generated.stream()
                .filter(s -> Long.valueOf(2L).equals(s.getSubjectId()))
                .sorted(Comparator.comparing(TimetableSlotResponseDTO::getPeriodIndex))
                .collect(Collectors.toList());
        assertEquals(3, edcLabSlots.size());

        DayOfWeek labDay = edcLabSlots.get(0).getDay();
        int p1 = edcLabSlots.get(0).getPeriodIndex();
        int p2 = edcLabSlots.get(1).getPeriodIndex();
        int p3 = edcLabSlots.get(2).getPeriodIndex();

        assertEquals(labDay, edcLabSlots.get(1).getDay(), "All 3 periods must be on same day");
        assertEquals(labDay, edcLabSlots.get(2).getDay(), "All 3 periods must be on same day");
        assertEquals(p1 + 1, p2, "Second period must immediately follow first");
        assertEquals(p1 + 2, p3, "Third period must immediately follow second");
    }

    @Test
    @DisplayName("Requirement 4: Two-period labs remain consecutive")
    void testTwoPeriodLabsRemainConsecutive() {
        Subject twoPeriodSubject = createSubject(20L, "LAB-2P", "Specialized Two-Period Lab", SubjectType.LAB);
        CourseOffering twoPeriodOffering = createOffering(120L, section, twoPeriodSubject, 4, 2, faculty1, ordinaryLab1);

        Subject theory1 = createSubject(21L, "TH-1", "Theory Subject 1", SubjectType.THEORY);
        CourseOffering theoryOffering = createOffering(121L, section, theory1, 4, 1, faculty2, defaultClassroom);

        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(List.of(twoPeriodOffering, theoryOffering));
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);
        assertEquals(8, generated.size(), "Total 8 periods (4 lab + 4 theory)");

        // Two-period lab has 4 weekly periods -> 2 sessions of 2 periods each
        List<TimetableSlotResponseDTO> labSlots = generated.stream()
                .filter(s -> Long.valueOf(20L).equals(s.getSubjectId()))
                .sorted(Comparator.comparing(TimetableSlotResponseDTO::getDay).thenComparing(TimetableSlotResponseDTO::getPeriodIndex))
                .collect(Collectors.toList());
        assertEquals(4, labSlots.size());

        // Group by day to verify each 2-period session is consecutive
        Map<DayOfWeek, List<TimetableSlotResponseDTO>> byDay = labSlots.stream()
                .collect(Collectors.groupingBy(TimetableSlotResponseDTO::getDay));
        assertEquals(2, byDay.size(), "Two 2-period sessions should be placed on 2 distinct days");

        for (Map.Entry<DayOfWeek, List<TimetableSlotResponseDTO>> entry : byDay.entrySet()) {
            List<TimetableSlotResponseDTO> daySlots = entry.getValue();
            assertEquals(2, daySlots.size());
            assertEquals(daySlots.get(0).getPeriodIndex() + 1, daySlots.get(1).getPeriodIndex(),
                    "2-period lab on " + entry.getKey() + " must be consecutive");
        }
    }

    @Test
    @DisplayName("Requirement 5: Lunch break (between P4 and P5) is never occupied or crossed")
    void testLunchBreakNeverOccupiedOrCrossed() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);

        // Multi-period sessions must not span across P4 (12:40) and P5 (13:30)
        // Group by day and check multi-period spans
        Map<DayOfWeek, List<TimetableSlotResponseDTO>> byDay = generated.stream()
                .collect(Collectors.groupingBy(TimetableSlotResponseDTO::getDay));

        for (Map.Entry<DayOfWeek, List<TimetableSlotResponseDTO>> entry : byDay.entrySet()) {
            List<TimetableSlotResponseDTO> daySlots = entry.getValue();
            for (TimetableSlotResponseDTO slot : daySlots) {
                if (slot.getSpanPeriods() > 1 && slot.isMultiPeriodHead()) {
                    int start = slot.getPeriodIndex();
                    int end = start + slot.getSpanPeriods() - 1;
                    assertFalse(start <= 4 && end >= 5,
                            "Multi-period session on " + entry.getKey() + " starting at " + start +
                            " and ending at " + end + " crosses the lunch break!");
                }
            }
        }
    }

    @Test
    @DisplayName("Requirement 6: No faculty or room conflicts are introduced")
    void testNoFacultyOrRoomConflictsIntroduced() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);

        // Section uniqueness per (day, period)
        Set<String> sectionSlots = new HashSet<>();
        // Faculty uniqueness per (faculty, day, period)
        Set<String> facultySlots = new HashSet<>();
        // Room uniqueness per (room, day, period)
        Set<String> roomSlots = new HashSet<>();

        for (TimetableSlotResponseDTO s : generated) {
            String secKey = s.getDay() + ":" + s.getPeriodIndex();
            assertTrue(sectionSlots.add(secKey), "Duplicate section slot at " + secKey);

            if (s.getFacultyId() != null) {
                String facKey = s.getFacultyId() + ":" + s.getDay() + ":" + s.getPeriodIndex();
                assertTrue(facultySlots.add(facKey), "Faculty conflict at " + facKey);
            }

            if (s.getRoomId() != null) {
                String rmKey = s.getRoomId() + ":" + s.getDay() + ":" + s.getPeriodIndex();
                assertTrue(roomSlots.add(rmKey), "Room conflict at " + rmKey);
            }
        }
    }

    @Test
    @DisplayName("Requirement 7: Soft preferences gracefully relax when ideal distribution is impossible")
    void testSoftPreferencesRelaxWhenIdealDistributionImpossible() {
        // Construct a scenario where Faculty 1 teaches a 3-period Theory subject,
        // but existing slots from other sections occupy Faculty 1 on Tuesday through Saturday.
        // Faculty 1 is ONLY available on Monday.
        // Ideal distribution (3 different days) is impossible, so the solver must relax
        // the preference and schedule sessions on Monday without throwing an exception.
        Subject tightTheory = createSubject(30L, "TIGHT-TH", "Constrained Theory", SubjectType.THEORY);
        CourseOffering tightOffering = createOffering(130L, section, tightTheory, 3, 1, faculty1, defaultClassroom);

        // Another section (ID 2)
        Section otherSection = new Section("ECE-B", "II Year ECE Section B", 60, null, null, null, defaultClassroom);
        otherSection.setId(2L);

        // Block Faculty 1 on Tue, Wed, Thu, Fri, Sat for all 7 periods in existingSlots
        List<TimetableSlot> blockingSlots = new ArrayList<>();
        List<DayOfWeek> blockedDays = List.of(
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY
        );
        for (DayOfWeek day : blockedDays) {
            for (int p = 1; p <= 7; p++) {
                blockingSlots.add(new TimetableSlot(otherSection, day, p, 1, false, false, tightTheory, faculty1, defaultClassroom));
            }
        }

        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(List.of(tightOffering));
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(blockingSlots);

        // Execution must succeed and produce a valid schedule despite the constraint
        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);
        assertEquals(3, generated.size(), "Must successfully schedule all 3 periods of TIGHT-TH");

        // All 3 periods should fall on MONDAY since all other days were blocked for Faculty 1
        for (TimetableSlotResponseDTO slot : generated) {
            assertEquals(DayOfWeek.MONDAY, slot.getDay(),
                    "Soft preference should have relaxed to allow multiple sessions on Monday");
        }
    }

    @Test
    @DisplayName("Requirement 8: Room compatibility and Technical Training exclusivity remain intact")
    void testRoomCompatibilityAndTrainingExclusivityPreserved() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);

        for (TimetableSlotResponseDTO slot : generated) {
            Long subId = slot.getSubjectId();
            Long roomId = slot.getRoomId();

            if (Long.valueOf(1L).equals(subId)) {
                // Technical Training -> Must be in E-701 (ID 102)
                assertEquals(102L, roomId, "Technical Training must be scheduled in E-701");
            } else if (Long.valueOf(5L).equals(subId)) {
                // Integrated Project-I -> Must be in Seminar Hall E-202 (ID 103)
                assertEquals(103L, roomId, "Integrated Project-I must be scheduled in E-202");
            } else if (List.of(2L, 3L, 4L).contains(subId)) {
                // Ordinary labs -> Must be in E-301, E-609, or E-104 (104, 105, 106), NEVER E-701 or E-202
                assertTrue(List.of(104L, 105L, 106L).contains(roomId),
                        "Ordinary labs must use ordinary lab rooms, but used " + roomId);
                assertNotEquals(102L, roomId, "Ordinary lab must NEVER be scheduled in exclusive room E-701");
                assertNotEquals(103L, roomId, "Ordinary lab must NEVER be scheduled in SEMINAR_HALL E-202");
            } else {
                // Theory courses -> Must use classroom E-201 (101), NEVER E-701 or E-202
                assertEquals(101L, roomId, "Theory subject must use classroom E-201");
                assertNotEquals(102L, roomId, "Theory subject must NEVER use exclusive training room E-701");
                assertNotEquals(103L, roomId, "Theory subject must NEVER use SEMINAR_HALL E-202");
            }
        }
    }

    @Test
    @DisplayName("Requirement 9: Multi-period lab blocks are distributed between morning (P1-P3) and afternoon (P5-P7)")
    void testMultiPeriodBlocksDistributedBetweenMorningAndAfternoon() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);

        // Find all multi-period head slots (duration 3 labs)
        List<TimetableSlotResponseDTO> multiPeriodHeads = generated.stream()
                .filter(s -> s.getSpanPeriods() >= 2 && s.isMultiPeriodHead())
                .collect(Collectors.toList());
        assertEquals(5, multiPeriodHeads.size(), "Should have 5 multi-period sessions (TT, EDC Lab, SS Lab, Java Lab, IP-I)");

        long morningLabCount = multiPeriodHeads.stream().filter(s -> s.getPeriodIndex() <= 4).count();
        long afternoonLabCount = multiPeriodHeads.stream().filter(s -> s.getPeriodIndex() >= 5).count();

        assertTrue(morningLabCount > 0, "Should have multi-period blocks in the morning (P1-P3)");
        assertTrue(afternoonLabCount > 0, "Should have multi-period blocks in the afternoon (P5-P7)");
        assertEquals(5, morningLabCount + afternoonLabCount, "All 5 multi-period blocks must be placed");
    }

    @Test
    @DisplayName("Requirement 10: Periods 6 and 7 are actively utilized across the weekly timetable")
    void testPeriods6And7AreUtilized() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);

        long p6Count = generated.stream().filter(s -> s.getPeriodIndex() == 6).count();
        long p7Count = generated.stream().filter(s -> s.getPeriodIndex() == 7).count();

        assertTrue(p6Count > 0, "Period 6 must have scheduled classes in the timetable");
        assertTrue(p7Count > 0, "Period 7 must have scheduled classes in the timetable");
    }

    @Test
    @DisplayName("Requirement 11: Theory classes can be scheduled in afternoon periods including P6")
    void testTheoryClassesCanUseLaterPeriods() {
        when(sectionService.findEntityById(1L)).thenReturn(section);
        when(courseOfferingRepository.findBySectionIdAndActiveTrue(1L)).thenReturn(buildStandardEceAOfferings());
        when(roomRepository.findAll()).thenReturn(allTestRooms());
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        List<TimetableSlotResponseDTO> generated = schedulerService.generateTimetable(1L);
        assertNotNull(generated);

        // Find single-period theory slots (IDs 6, 7, 8, 9, 10) in period 6
        List<TimetableSlotResponseDTO> theoryInP6 = generated.stream()
                .filter(s -> s.getSpanPeriods() == 1 && s.getPeriodIndex() == 6)
                .collect(Collectors.toList());

        assertFalse(theoryInP6.isEmpty(), "Theory classes should be able to utilize Period 6");
    }
}
